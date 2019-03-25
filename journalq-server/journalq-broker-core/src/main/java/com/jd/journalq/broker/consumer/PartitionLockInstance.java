package com.jd.journalq.broker.consumer;

import com.jd.journalq.broker.consumer.model.ConsumePartition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 分区锁
 * <br>
 * 用于降低锁的粒度，提升访问效率
 * <p>
 * Created by chengzhiliang on 2018/9/12.
 */
public class PartitionLockInstance {

    // K=消费分区，V=消费分区;用于按照每个消费分区加锁控制同步访问
    private ConcurrentMap<ConsumePartition, ConsumePartition> partitionLockCache = new ConcurrentHashMap<>();

    /**
     * @param topic     消息主题
     * @param app       消息应用
     * @param partition 消息分区
     * @return 锁实例
     */
    public ConsumePartition getLockInstance(String topic, String app, short partition) {
        ConsumePartition consumePartition = new ConsumePartition(topic, app, partition);
        return getLockInstance(consumePartition);
    }

    /**
     * @param consumePartition 消费分区
     * @return 锁实例
     */
    public ConsumePartition getLockInstance(ConsumePartition consumePartition) {
        ConsumePartition lock = partitionLockCache.get(consumePartition);
        // 如果获取的实例为null,则构造一个实例放入缓存Map中
        if (lock == null) {
            lock = consumePartition;
            ConsumePartition preLock =  partitionLockCache.putIfAbsent(consumePartition, consumePartition);
            if (preLock != null){
                lock = preLock;
            }
        }
        return lock;
    }


}
