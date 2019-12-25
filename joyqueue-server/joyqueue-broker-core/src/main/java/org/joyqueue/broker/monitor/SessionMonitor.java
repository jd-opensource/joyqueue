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
package org.joyqueue.broker.monitor;

import org.joyqueue.monitor.Client;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.session.Producer;

import java.util.List;

public interface SessionMonitor {


    /**
     * 增加生产者
     * @param producer 生产者
     */
    void addProducer(Producer producer);

    /**
     * 增加Consumer
     * @param consumer 消费者对象
     */
    void addConsumer(Consumer consumer);

    /**
     * 移除生产者
     * @param producer 生产者
     */
    void removeProducer(Producer producer);

    /**
     * 移除消费者
     * @param consumer 消费者
     */
    void removeConsumer(Consumer consumer);

    /**
     * 获取当前生产者数量
     *
     * @param topic 主题
     * @param app   应用
     * @return 当前生产者数量
     */
    int getProducer(String topic, String app);

    /**
     * 获取当前消费者数量
     *
     * @param topic 主题
     * @param app   应用
     * @return 当前消费者数量
     */
    int getConsumer(String topic, String app);

    /**
     * 增加连接明细
     *
     * @param connection 连接内容
     */
    void addConnection(Connection connection);

    /**
     * 移除连接明细
     *
     * @param connection 连接内容
     */
    void removeConnection(Connection connection);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     * @param app   应用
     */
    List<Client> getConnections(String topic, String app);
}
