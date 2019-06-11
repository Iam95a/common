package com.chen.common.im.spring.service.redis.impl;


import com.chen.common.im.SysLogger;
import com.chen.common.im.spring.service.RedisService;
import com.chen.common.im.spring.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public long incr(String key, long value) {
        return redisTemplate.opsForValue().increment(key, value);
    }

    @Override
    public long decr(String key, long value) {
        return redisTemplate.opsForValue().increment(key, -value);
    }

    @Override
    public boolean set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            SysLogger.error(getClass(), "Redis set error!", e);
            return false;
        }
        return true;
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        return ops.entries(key);
    }

    @Override
    public void hmset(String key, Map<String, String> map) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        ops.putAll(key, map);
    }


    @Override
    public void hset(String key, String field, String value) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        ops.put(key, field, value);
    }

    @Override
    public void hdel(String key, String... field) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        ops.delete(key, field);
    }

    @Override
    public String hget(String key, String field) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        return ops.get(key, field);
    }

    @Override
    public long hincrby(String key, String field, int value) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        return ops.increment(key, field, value);
    }

    @Override
    public long scard(String key) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.size(key);
    }

    @Override
    public long sadd(String key, String value) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.add(key, value);
    }

    @Override
    public long sadd(String key, Collection<String> collection) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.add(key, collection.toArray(new String[collection.size()]));
    }

    @Override
    public long srem(String key, String value) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.remove(key, value);
    }

    @Override
    public long srem(String key, Collection<String> collection) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.remove(key, collection.toArray(new String[collection.size()]));
    }

    @Override
    public Set<String> smembers(String key) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.members(key);
    }

    @Override
    public List<String> srandmember(String key, long count) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.randomMembers(key, count);
    }

    @Override
    public Set<String> distinctsrandmember(String key, long count) {
        SetOperations<String, String> ops = redisTemplate.opsForSet();
        return ops.distinctRandomMembers(key, count);
    }

    @Override
    public boolean getLock(String key, Object id) {
        return getLock(key, id, 5);
    }

    @Override
    public boolean getLock(String key, Object id, int exp) {
        String cacheKey = key + id;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(cacheKey, "1");
        if (success != null && success) {
            redisTemplate.expire(cacheKey, exp, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    @Override
    public void releaseLock(String key, Object id) {
        String cacheKey = key + id;
        redisTemplate.delete(cacheKey);
    }

    @Override
    public boolean setExpire(String key, String value, long expire) {
        try {
            redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        } catch (Exception e) {
            SysLogger.error(getClass(), "Redis setExpire error!", e);
            return false;
        }
        return true;
    }

    @Override
    public void lpush(String key, String value) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.leftPush(key, value);
    }

    @Override
    public void lpush(String key, List<String> values) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.rightPushAll(key, values);
    }

    @Override
    public String lpop(String key) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.leftPop(key);
    }

    @Override
    public Long llen(String key) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.size(key);
    }

    @Override
    public void ltrim(String key, int start, int stop) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.trim(key, start, stop);
    }

    @Override
    public String lindex(String key, long index) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.index(key, index);
    }

    @Override
    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    @Override
    public Boolean expire(String key, long expire) {
        return redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    @Override
    public long zadd(String key, Set<TypedTuple<String>> set) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.add(key, set);
    }

    @Override
    public boolean zadd(String key, String value, double score) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.add(key, value, score);

    }

    @Override
    public Double zscore(String key, String value) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.score(key, value);

    }

    @Override
    public double zincrby(String key, String value, double score) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.incrementScore(key, value, score);

    }

    @Override
    public void zrem(String key, String value) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.remove(key, value);
    }

    @Override
    public Long zReverseRank(String key, String value) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.rank(key, value);
    }

    @Override
    public long zzremrangebyscore(String key, double minScore, double maxScore) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.removeRangeByScore(key, minScore, maxScore);
    }

    @Override
    public Long zrevrank(String key, String value) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRank(key, value);
    }

    @Override
    public Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Boolean hasKey(final String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public DataType type(final String key) {
        return redisTemplate.type(key);
    }

    @Override
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.range(key, start, end);
    }

    @Override
    public void rpushAll(String key, Collection<String> value) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.rightPushAll(key, value);
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.range(key, start, end);
    }

    @Override
    public Set<String> zrevrange(String key, int i, int i1) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRange(key, i, i1);
    }

    @Override
    public Set<TypedTuple<String>> zrevrangewithscore(String key, int i, int i1) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRangeWithScores(key, i, i1);
    }

    @Override
    public Set<TypedTuple<String>> zrangeByScoreWithScores(String key, double min, double max) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.rangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<TypedTuple<String>> zReverseRangeByScoreWithScores(String key, double min, double max) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRangeByScoreWithScores(key, min, max);
    }

    @Override
    public long zcard(String key) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.size(key);
    }

    @Override
    public void lrem(String key, String value) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.remove(key, 0, value);
    }

    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void rename(String key, String newKey) {
        redisTemplate.rename(key, newKey);
    }

    @Override
    public void rpush(String key, String value) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.rightPush(key, value);
    }

    @Override
    public void rpushAll(String key, List<String> values) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.rightPushAll(key, values);
    }

    @Override
    public String rpop(String key) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.rightPop(key);
    }

    @Override
    public List<String> hmultiget(String key, List<String> fields) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        return ops.multiGet(key, fields);
    }

    @Override
    public Long pfadd(String key, String element) {
        HyperLogLogOperations<String, String> ops = redisTemplate.opsForHyperLogLog();
        return ops.add(key, element);
    }

    @Override
    public Long pfcount(String key) {
        HyperLogLogOperations<String, String> ops = redisTemplate.opsForHyperLogLog();
        return ops.size(key);
    }


}
