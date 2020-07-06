/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.consumer;

import org.joyqueue.broker.consumer.model.ConsumePartition;

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
