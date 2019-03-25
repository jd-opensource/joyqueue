package com.jd.journalq.broker.consumer.position;

import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.Iterator;

/**
 * 消费位点存储接口
 * <p>
 * Created by chengzhiliang on 2019/2/27.
 */
public interface PositionStore<K, V> extends LifeCycle {

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    V get(K key);

    /**
     * 存储值
     *
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     * 移除值
     *
     * @param key
     * @return
     */
    V remove(K key);

    /**
     * 存储值如果不存在这个键
     *
     * @param key
     * @param value
     * @return
     */
    V putIfAbsent(K key, V value);

    /**
     * 强制刷盘（针对存储加缓存的实现）
     */
    void forceFlush();

    /**
     * 获取key迭代器
     *
     * @return 存储中key的迭代器
     */
    Iterator<K> iterator();


}
