package com.chen.common.im.spring.service.redis;

import com.chen.common.im.spring.dto.RedisBatchRequest;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisBatchService {

    List<Map<String, String>> hgetall(Collection<String> keys);

    Map<String, Map<String, String>> hmget(final String keyName, final List<String> keys, final List<String> fields);

    void hincrby(List<RedisBatchRequest.HincrbyRequest> requests);

    /**
     * 批量减少
     * @param requests
     * @return 剩余
     */
    List<Object> hdecrby(List<RedisBatchRequest.HincrbyRequest> requests);

    /**
     * 从列表key中取出 batchSize个
     * @param key
     * @param batchSize
     * @return
     */
    List<Object> lpopbatch(String key, int batchSize);

    /**
     * 批量操作zset
     * @param key
     * @param set
     */
    void zincrby(String key, Set<ZSetOperations.TypedTuple<String>> set);

    /**
     * 批量获取zRank
     * @param key
     * @param set
     * @return
     */
    List<Object> zRevRank(String key, List<Long> set);

}
