package com.chen.common.im.spring.service.redis.impl;


import com.chen.common.im.spring.dto.RedisBatchRequest;
import com.chen.common.im.spring.service.redis.RedisBatchService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author : Richard
 * @Description :
 * @Date : 2018/6/6
 */
@Service
public class RedisBatchServiceImpl implements RedisBatchService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Map<String, String>> hgetall(final Collection<String> keys) {
        final RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();

        return redisTemplate.execute(new RedisCallback<List<Map<String, String>>>() {
            @Override
            public List<Map<String, String>> doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                boolean pipelinedClosed = false;

                try {
                    for (String key : keys) {
                        connection.hGetAll(keySerializer.serialize(key));
                    }

                    List<Object> closePipeline = connection.closePipeline();
                    pipelinedClosed = true;
                    return deserializeHashResults(closePipeline);
                } finally {
                    if (!pipelinedClosed) {
                        connection.closePipeline();
                    }

                }
            }
        });
    }

    @Override
    public Map<String, Map<String, String>> hmget(final String keyName, final List<String> keys, final List<String> fields) {
        final RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();

        final byte[][] fieldBytes = new byte[fields.size()][];
        int i = 0;
        for (String field : fields) {
            fieldBytes[i] = stringSerializer.serialize(field);
            i++;
        }

        return redisTemplate.execute(new RedisCallback<Map<String, Map<String, String>>>() {
            @Override
            public Map<String, Map<String, String>> doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                boolean pipelinedClosed = false;

                try {
                    for (String key : keys) {
                        connection.hMGet(stringSerializer.serialize(key), fieldBytes);
                    }

                    List<Object> closePipeline = connection.closePipeline();
                    pipelinedClosed = true;
                    return deserializeListResults(keyName, fields, closePipeline, stringSerializer);
                } finally {
                    if (!pipelinedClosed) {
                        connection.closePipeline();
                    }

                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> deserializeListResults(String key, List<String> fields,
                                                                    @Nullable List<Object> rawValues,
                                                                    RedisSerializer<String> stringSerializer) {
        if (rawValues == null) {
            return null;
        } else {
            Map<String, Map<String, String>> values = new HashMap<>(rawValues.size());

            for (Object rawValue : rawValues) {
                if (rawValue != null && rawValue instanceof List && !((List) rawValue).isEmpty() && ((List) rawValue)
                        .iterator().next() instanceof byte[]) {
                    Map<String, String> map = new HashMap<>(fields.size());
                    for (int i = 0; i < fields.size(); i++) {
                        map.put(fields.get(i), stringSerializer.deserialize((byte[]) ((List) rawValue).get(i)));
                    }
                    if (map.containsKey(key)) {
                        values.put(map.get(key), map);
                    }
                }
            }

            return values;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> deserializeHashResults(String key, @Nullable List<Object> rawValues) {
        if (rawValues == null) {
            return null;
        } else {
            Map<String, Map<String, String>> values = new HashMap<>(rawValues.size());

            for (Object rawValue : rawValues) {
                if (rawValue != null && rawValue instanceof Map && !((Map) rawValue).isEmpty() && ((Map) rawValue)
                        .values().iterator().next() instanceof byte[]) {
                    Map<String, String> map = SerializationUtils.deserialize((Map) rawValue, redisTemplate
                            .getHashKeySerializer(), redisTemplate.getHashValueSerializer());
                    if (map.containsKey(key)) {
                        values.put(map.get(key), map);
                    }
                }
            }

            return values;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> deserializeHashResults(@Nullable List<Object> rawValues) {
        if (rawValues == null) {
            return null;
        } else {
            List<Map<String, String>> values = new ArrayList<>(rawValues.size());

            for (Object rawValue : rawValues) {
                if (rawValue != null && rawValue instanceof Map && !((Map) rawValue).isEmpty() && ((Map) rawValue)
                        .values().iterator().next() instanceof byte[]) {
                    values.add(SerializationUtils.deserialize((Map) rawValue, redisTemplate.getHashKeySerializer(),
                            redisTemplate.getHashValueSerializer()));
                } else {
                    values.add(null);
                }
            }

            return values;
        }
    }

    @Override
    public void hincrby(List<RedisBatchRequest.HincrbyRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (RedisBatchRequest.HincrbyRequest request : requests) {
                    if (StringUtils.isAnyBlank(request.getKey(), request.getField()) || request.getValue() == 0) {
                        continue;
                    }
                    connection.hIncrBy(request.getKey().getBytes(), request.getField().getBytes(), request.getValue());
                }
                return null;
            }
        });
    }

    @Override
    public List<Object> hdecrby(List<RedisBatchRequest.HincrbyRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return null;
        }
        return redisTemplate.execute(new RedisCallback<List<Object>>() {
            @Override
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                try {
                    List<Map<String, String>> list = new ArrayList<>(requests.size());
                    for (RedisBatchRequest.HincrbyRequest request : requests) {
                        if (StringUtils.isAnyBlank(request.getKey(), request.getField())) {
                            continue;
                        }
                        connection.hIncrBy(request.getKey().getBytes(), request.getField().getBytes(), -request
                                .getValue());
                    }

                    return connection.closePipeline();
                } finally {
                    connection.closePipeline();
                }
            }
        });
    }

    @Override
    public List<Object> lpopbatch(String key, int batchSize) {
        List<Object> results = stringRedisTemplate.executePipelined(
                new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(RedisConnection connection) throws DataAccessException {
                        StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                        for (int i = 0; i < batchSize; i++) {
                            stringRedisConn.lPop(key);
                        }
                        return null;
                    }
                });
        return results;
    }

    @Override
    public void zincrby(String key, Set<ZSetOperations.TypedTuple<String>> set) {
        if (StringUtils.isBlank(key) || CollectionUtils.isEmpty(set)) {
            return;
        }
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (ZSetOperations.TypedTuple<String> zTypedTuple : set) {
                    if (!ObjectUtils.allNotNull(zTypedTuple.getValue(), zTypedTuple.getScore()) || zTypedTuple
                            .getScore() == 0) {
                        continue;
                    }
                    connection.zIncrBy(key.getBytes(), zTypedTuple.getScore(), zTypedTuple.getValue().getBytes());
                }
                return null;
            }
        });
    }

    @Override
    public List<Object> zRevRank(String key, List<Long> set) {
        final RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
        return redisTemplate.execute(new RedisCallback<List<Object>>() {
            @Override
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                boolean pipelinedClosed = false;

                try {
                    for (Long id : set) {
                        connection.zRevRank(stringSerializer.serialize(key), stringSerializer.serialize(id.toString()));
                    }

                    List<Object> closePipeline = connection.closePipeline();
                    pipelinedClosed = true;
                    return closePipeline;
                } finally {
                    if (!pipelinedClosed) {
                        connection.closePipeline();
                    }

                }
            }
        });
    }
}
