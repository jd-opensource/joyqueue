package com.jd.journalq.common.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存操作的插件接口
 */
public interface CacheService {
    /**
     * 追加到现有键对应列表中
     *
     * @param key      键
     * @param value    值
     * @param maxCount 列表最大长度，如超过最大长度，将移除最早的元素
     */
    void rpush(String key, String value, int maxCount);

    /**
     * 追加到现有键对应列表中
     *
     * @param key      键
     * @param value    值
     * @param maxCount 列表最大长度，如超过最大长度，将移除最早的元素
     */
    void rpush(byte[] key, byte[] value, int maxCount);

    /**
     * 取出对应键中相应范围内的值
     *
     * @param key  键
     * @param from 开始索引
     * @param to   结束索引
     * @return 值的列表
     */
    List<String> range(String key, int from, int to);

    /**
     * 取出对应键中相应范围内的值
     *
     * @param key  键
     * @param from 开始索引
     * @param to   结束索引
     * @return 值的列表
     */
    List<byte[]> range(byte[] key, int from, int to);

    /**
     * 增加键值对
     *
     * @param key   键
     * @param value 值
     */
    void put(String key, String value);

    /**
     * 获取对应键的值
     *
     * @param key 键
     * @return 值
     */
    String get(String key);

    /**
     * 获取对应键的值
     *
     * @param key 键
     * @return 值
     */
    byte[] get(byte[] key);

    /**
     * 原子性的将对应键的值增加1
     *
     * @param key 键
     * @return 自增完成的值
     */
    Long incr(String key);

    /**
     * 原子性的将对应键的值增加指定值
     *
     * @param key   键
     * @param count 值
     * @return 自增完成的值
     */
    Long incrBy(String key, long count);

    /**
     * 原子性的将对应键的值减去1
     *
     * @param key 键
     * @return 自增完成的值
     */
    Long decr(String key);

    /**
     * 原子性的将对应键的值减去指定值
     *
     * @param key   键
     * @param count 值
     * @return 自增完成的值
     */
    Long decrBy(String key, long count);

    /**
     * 删除对应的键值对
     *
     * @param key 键
     */
    void delete(String key);

    /**
     * 删除对应的键值对
     *
     * @param key 键
     */
    void delete(byte[] key);

    /**
     * 设置
     *
     * @param key   键
     * @param value 值
     * @return
     */
    void set(String key, String value);

    /**
     * 设置有效期键值
     *
     * @param key     键
     * @param seconds 有效期
     * @param value   值
     * @return
     */
    void setex(byte[] key, int seconds, byte[] value);

    /**
     * 设置有效期键值
     *
     * @param key     键
     * @param seconds 有效期
     * @param value   值
     * @return
     */
    void setex(String key, int seconds, String value);

    /**
     * 添加sorted set
     *
     * @param key    键
     * @param score  分数
     * @param member 值
     */
    void zadd(String key, double score, String member);

    /**
     * 通过位置返回sorted set指定区间内的成员
     *
     * @param key   键
     * @param start 其实位置
     * @param end   结束位置
     * @return 返回所有符合条件的成员
     */
    Set<String> zrange(String key, long start, long end);

    /**
     * 通过分数返回sorted set指定区间内的成员
     *
     * @param key 键
     * @param min 最小评分
     * @param max 最大评分
     * @return 返回所有符合条件的成员
     */
    Set<String> zrangeByScore(String key, double min, double max);

    /**
     * 通过分数返回sorted set指定区间内的成员
     *
     * @param key    键
     * @param min    最小评分
     * @param max    最大评分
     * @param offset 偏移位置
     * @param count  返回数量
     * @return 返回所有符合条件的成员
     */
    Set<String> zrangeByScore(String key, double min, double max, long offset, long count);

    /**
     * 统计score在min和max之间的成员数
     *
     * @param key 键
     * @param min 最小score
     * @param max 最大score
     * @return 符合条件的成员数
     */
    Long zcount(String key, double min, double max);

    /**
     * 统计成员数量
     *
     * @param key
     * @return
     */
    Long zcard(String key);

    /**
     * 移除sorted set中的一个或多个成员
     *
     * @param key    键
     * @param member 值
     * @return 被成功移除的成员的数量，不包括被忽略的成员
     */
    Long zrem(String key, String... member);

    /**
     * 获取指定成员的评分
     *
     * @param key    键
     * @param member 值
     * @return 评分，不存在则返回null
     */
    Double zscore(String key, String member);

    /**
     * 如果不存在则设置
     *
     * @param key
     * @param value
     * @return 是否成功
     */
    Boolean setnx(String key, String value);

    /**
     * 设置过期时间
     *
     * @param key         键
     * @param millisecond 毫秒
     * @return 是否成功
     */
    Boolean expire(String key, int millisecond);

    /**
     * 获取key的剩余生存时间
     *
     * @param key 键
     * @return 当 key 不存在时，返回 -2
     * 当 key 存在但没有设置剩余生存时间时，返回 -1
     * 否则，以秒为单位，返回 key 的剩余生存时间
     */
    Long ttl(String key);

    /**
     * 移出并获取列表的第一个元素
     *
     * @param key 键
     * @return 第一个元素
     */
    String lpop(String key);

    /**
     * 在列表中添加一个或多个值
     *
     * @param key   键
     * @param value 值
     * @return 最新列表长度
     */
    Long rpush(String key, String... value);

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 长度
     */
    Long llen(String key);

    /**
     * 从key对应list中删除count个和value相同的元素
     *
     * @param key
     * @param count count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
     *              count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
     *              count = 0 : 移除表中所有与 value 相等的值。
     * @param value
     * @return 被移除的个数
     */
    Long lrem(String key, final long count, String value);

    /**
     * 移除并返回集合中的一个随机元素
     *
     * @param key
     * @return
     */
    String spop(String key);

    /**
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
     * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
     *
     * @param key
     * @param values
     * @return 被添加到集合中的新元素的数量，不包括被忽略的元素
     */
    Long sadd(String key, String... values);

    /**
     * 返回集合 key 的基数(集合中元素的数量)。
     *
     * @param key
     * @return 当 key 不存在时，返回 0
     */
    Long scard(String key);

    /**
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
     * 当 key 不是集合类型，返回一个错误。
     *
     * @param key
     * @param values
     * @return 被成功移除的元素的数量，不包括被忽略的元素。
     */
    Long srem(String key, String... values);

    /**
     * 获取委托的缓存
     *
     * @return
     */
    CacheService getDelegate();

    /**
     * 将Key-Value 写入指定的Map，如果Key已存在，则覆盖
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    boolean hSet(String key, String field, String value);

    /**
     * 从Map中获取指定Key的Value
     *
     * @param key
     * @param field
     * @return
     */
    String hGet(String key, String field);

    /**
     * 删除Map中指定的Key对应的Key-Value
     *
     * @param key
     * @param fields
     * @return
     */
    Long hDel(String key, String... fields);

    /**
     * 获取指定Map的所有的Key-Value
     *
     * @param key
     * @return
     */
    Map hGetAll(String key);
}
