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
package org.joyqueue.broker.consumer.position;

import org.joyqueue.toolkit.lang.LifeCycle;

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
