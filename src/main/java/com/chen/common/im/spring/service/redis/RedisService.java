package com.chen.common.im.spring.service.redis;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {
    /** key-value operation */
    long incr(String key, long value);

    long decr(String key, long value);

    boolean set(String key, String value);

    String get(String key);

    boolean getLock(String key, Object id);

    boolean getLock(String key, Object id, int exp);

    void releaseLock(String key, Object id);

    Boolean del(String key);

    Boolean hasKey(String key);

    DataType type(String key);

    Boolean persist(String key);

    /** Hash operation */
    Map<String, String> hgetAll(String key);

    void hmset(String key, Map<String, String> map);

    void hset(String key, String field, String value);

    /**删除map中的某个键*/
    void hdel(String key,String... field);

    long hincrby(String key, String field, int value);

    String hget(String key, String field);


    /** Set operation */
    long scard(String key);

    long sadd(String key, String value);

    long sadd(String key, Collection<String> collection);

    long srem(String key, String value);

    long srem(String key, Collection<String> collection);

    Set<String> smembers(String key);

    /**
     * 这里spring data redis和redis本身处理不一样   count要为正  底层代码会取负值导致取得随机值不唯一
     * @param key
     * @param count
     * @return
     */
    List<String> srandmember(String key, long count);

    /**
     * 随机出不重复的值
     * @param key
     * @param count
     * @return
     */
    Set<String> distinctsrandmember(String key,long count);

    /**
     * 设置k-v以及超时时间
     * @param expire 超时时间  秒
     * @return 设置成功返回true  否则false
     */
    boolean setExpire(String key, String value, long expire);

    /**
     * list lpush方法
     * @param key   key
     * @param value 要push的值
     */
    void lpush(String key, String value);

    /**
     * list lpush方法
     * @param key
     * @param values
     */
    void lpush(String key, List<String> values);

    /**
     * list lpop方法
     * @param key   key
     */
    String lpop(String key);

    /**
     * list llen方法  获取list长度
     * @param key key
     * @return key对应的list的长度
     */
    Long llen(String key);

    /**
     * list ltrim方法  对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     * @param key
     * @return key对应的list的长度
     */
    void ltrim(String key, int start, int stop);

    /**
     * list 获取key对应index的值
     * @param key   key
     * @param index index
     * @return 值
     */
    String lindex(String key, long index);

    /**
     * 推入消息到redis消息通道
     * @param channel  要监听的消息通道
     * @param message  消息
     */
    void publish(String channel, Object message);

    /**
     * 为key设置超时时间
     * @param key    key
     * @param expire 超时时间  单位秒
     * @return 设置成功返回true 否则返回false
     */
    Boolean expire(String key, long expire);

    /**
     * 向sortedSet中添加数据
     * @param key key
     * @param set set
     * @return 添加成功返回true 否则返回false
     */
    long zadd(String key, Set<ZSetOperations.TypedTuple<String>> set);

    /**
     * 向sortedSet中添加单条数据
     * @param key
     * @param value
     * @param score
     * @return
     */
    boolean zadd(String key, String value,double score);

    /**
     * 返回有序集中，成员的分数值
     * @param key
     * @param value
     * @return
     */
    Double zscore(String key, String value);

    double zincrby(String key, String value, double score);

    /**
     * 对应redis zrem方法
     * @param key
     * @param value
     */
    void zrem(String key, String value);

    /**
     * 获取zset成员的排序号 倒叙
     * @param key
     * @param value
     */
    Long zReverseRank(String key, String value);

    /**
     * 删除分数区间的zSet value
     * @param key      key
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @return 删除了几项就会返回相应的数值
     */
    long zzremrangebyscore(String key, double minScore, double maxScore);

    /**
     * 返回有序集合中指定成员的排名，有序集成员按分数值递减(从大到小)排序
     * @param key
     * @param value
     * @return
     */
    Long zrevrank(String key, String value);

    /**
     * 取出范围内的元素
     * @param key   键
     * @param start 开始
     * @param end   结束
     * @return
     */
    List<String> lrange(String key, long start, long end);

    /**
     * 将列表元素一次性push到redis中去
     * @param key
     * @param value
     */
    void rpushAll(String key, Collection<String> value);

    /**
     * 取出范围内的元素
     * @param key   键
     * @param start 开始
     * @param end   结束
     * @return
     */
    Set<String> zrange(String key, long start, long end);

    /**
     * 按分数从高到低排列
     * @param key
     * @param i
     * @param i1
     * @return
     */
    Set<String> zrevrange(String key, int i, int i1);

    /**
     * 按分数从高到低排序，带分数
     * @param key
     * @param i
     * @param i1
     * @return
     */
    Set<ZSetOperations.TypedTuple<String>> zrevrangewithscore(String key, int i, int i1);

    Set<ZSetOperations.TypedTuple<String>> zrangeByScoreWithScores(String key, double min, double max);

    /**
     * 区间获取有序集合中的成员(带分数从大到小)
     * @param key
     * @param min
     * @param max
     * @return
     */
    Set<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores(String key, double min, double max);

    /**
     * 获取有序集合中的成员数
     * @param key
     * @return
     */
    long zcard(String key);

    /**
     * 列表中删除某个值
     * @param key
     * @param value
     */
    void lrem(String key,String value);

    boolean exists(String key);

    void rename(String key, String newKey);

    void rpush(String key, String value);

    void rpushAll(String key, List<String> values);

    String rpop(String key);

    List<String> hmultiget(String key, List<String> fields);

    /**
     * 如果至少有个元素被添加返回1, 否则返回0
     */
    Long pfadd(String key, String element);

    Long pfcount(String key);

}
