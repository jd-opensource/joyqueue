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

import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 会话管理
 * User: weiqisong
 * Date: 14-4-23
 * Time: 上午10:54
 * 该类负责连接，生产者，消费者信息管理。
 */
// TODO 优化getConsumerByTopic
public class SessionManager extends Service {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    // 绑定的连接Key
    public static final String CONNECTION_KEY = "connection";
    // 绑定的用户Key
    public static final String USER_KEY = "user";

    // 保存连接信息
    private ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();
    // 保存生产者信息
    private ConcurrentMap<String, Producer> producers = new ConcurrentHashMap<>();
    // 保存消费者信息
    private ConcurrentMap<String, Consumer> consumers = new ConcurrentHashMap<>();
    // 事件管理器
    private EventBus<SessionEvent> eventManager = new EventBus<>("joyqueue-frontend-session-eventBus");

    @Override
    protected void doStart() throws Exception {
        eventManager.start();
        logger.info("session manager is started");
    }

    @Override
    protected void doStop() {
        eventManager.stop();
        logger.info("session manager is stopped");
    }

    /**
     * 根据topic查询consumer
     * @return
     */
    public Consumer getConsumerByTopic(String connectionId, String topic) {
        for (Map.Entry<String, Consumer> entry : consumers.entrySet()) {
            Consumer consumer = entry.getValue();
            if (consumer.getConnectionId().equals(connectionId) && consumer.getTopic().equals(topic)) {
                return consumer;
            }
        }
        return null;
    }

    /**
     * 添加链接
     *
     * @param connection 连接
     * @return <li>true 成功</li>
     * <li>false 已经存在</li>
     */
    public boolean addConnection(final Connection connection) {
        if (connection == null) {
            return false;
        }
        if (connections.putIfAbsent(connection.getId(), connection) == null) {
            eventManager.inform(new SessionEvent(SessionEventType.AddConnection, connection));
            if (logger.isDebugEnabled()) {
                logger.debug("add connection :" + connection.getId());
            }
            return true;
        }
        return false;
    }

    /**
     * 删除链接
     *
     * @param connectionId 连接ID
     */
    public void removeConnection(final String connectionId) {
        Connection connection = connections.remove(connectionId);
        if (connection == null) {
            return;
        }
        removeConsumer(connection);
        removeProducer(connection);

        eventManager.add(new SessionEvent(SessionEventType.RemoveConnection, connection));

    }

    /**
     * 根据连接删除生产者
     *
     * @param connection 连接
     */
    protected void removeProducer(final Connection connection) {
        if (connection == null) {
            return;
        }

        for (Map.Entry<String, ConcurrentMap<String, String>> entry : connection.getProducers().entrySet()) {
            for (Map.Entry<String, String> producerEntry : entry.getValue().entrySet()) {
                Producer producer = producers.remove(producerEntry.getValue());
                if (producer != null) {
                    eventManager.add(new SessionEvent(SessionEventType.RemoveProducer, producer));
                }
            }
        }
    }

    /**
     * 根据连接删除消费者
     *
     * @param connection
     */
    protected void removeConsumer(final Connection connection) {
        if (connection == null) {
            return;
        }

        for (Map.Entry<String, ConcurrentMap<String, String>> entry : connection.getConsumers().entrySet()) {
            for (Map.Entry<String, String> consumerEntry : entry.getValue().entrySet()) {
                Consumer consumer = consumers.remove(consumerEntry.getValue());
                if (consumer != null) {
                    eventManager.add(new SessionEvent(SessionEventType.RemoveConsumer, consumer));
                }
            }
        }
    }

    /**
     * 返回所有连接。
     *
     * @return 连接列表
     */
    public List<Connection> getConnection() {
        return new ArrayList<Connection>(connections.values());
    }

    /**
     * 获取链接
     *
     * @param id 链接id
     * @return 连接
     */
    public Connection getConnectionById(final String id) {
        return connections.get(id);
    }

    /**
     * 添加consumer
     *
     * @param consumer 消费者
     * @return <li>true 成功</li>
     * <li>false 失败：消费者已经存在或连接不存在</li>
     */
    public boolean addConsumer(final Consumer consumer) {
        if (consumer == null) {
            return false;
        }

        Connection connection = getConnectionById(consumer.getConnectionId());
        if (connection == null) {
            return false;
        }

        if (connection.addConsumer(consumer.getTopic(), consumer.getApp(), consumer.getId())
                && consumers.putIfAbsent(consumer.getId(), consumer) == null) {
            try {
                // 成功则同步通知监听器
                eventManager.inform(new SessionEvent(SessionEventType.AddConsumer, consumer));
            } catch (Exception e) {
                logger.error("通知增加消费者失败," + e.getMessage(), e);
                removeConsumer(consumer.getId());
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("add consumer :" + consumer.getId());
            }
            return true;
        } else {
            logger.warn(String.format("consumer is already exists. topic=%s app=%s id=%s", consumer.getTopic(),
                    consumer.getApp(), consumer.getId()));
        }
        return true;
    }

    /**
     * 删除consumer
     *
     * @param consumerId 消费者ID
     */
    public boolean removeConsumer(final String consumerId) {
        Consumer consumer = consumers.remove(consumerId);
        if (consumer == null) {
            return false;
        }

        Connection connection = getConnectionById(consumer.getConnectionId());
        if (connection == null) {
            return false;
        }

        connection.removeConsumer(consumer.getTopic(), consumer.getApp());
        // 异步通知
        eventManager.add(new SessionEvent(SessionEventType.RemoveConsumer, consumer));
        return true;
    }

    /**
     * 获取所有的consumer
     *
     * @return 消费者列表
     */
    public List<Consumer> getConsumer() {
        return new ArrayList<Consumer>(consumers.values());
    }

    /**
     * 获取所有的 Producers
     * @return 生产者列表
     */
    public List<Producer> getProducer() {
        return new ArrayList<Producer>(producers.values());
    }

    /**
     * 根据ID查找消费者
     *
     * @param id 消费者ID
     * @return 匹配的消费者
     */
    public Consumer getConsumerById(final String id) {
        return consumers.get(id);
    }

    /**
     * 添加生产者
     *
     * @param producer 生产者
     * @return <li>true 成功</li>
     * <li>false 失败:连接不存在或生产者已经存在</li>
     */
    public boolean addProducer(final Producer producer) {
        if (producer == null) {
            return false;
        }

        Connection connection = getConnectionById(producer.getConnectionId());
        if (connection == null) {
            return false;
        }

        if (connection.addProducer(producer.getTopic(), producer.getApp(), producer.getId())
                && producers.putIfAbsent(producer.getId(), producer) == null) {
            try {
                eventManager.inform(new SessionEvent(SessionEventType.AddProducer, producer));
            } catch (Exception e) {
                removeProducer(producer.getId());
                logger.error("通知生产者增加失败," + e.getMessage(), e);
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("add producer" + producer.getId());
            }
            return true;
        } else {
            logger.warn(String.format("producer is already exists. topic=%s app=%s id=%s", producer.getTopic(),
                    producer.getApp(), producer.getId()));
        }
        return true;
    }

    /**
     * 删除生产者
     *
     * @param producerId 消费者ID
     */
    public boolean removeProducer(final String producerId) {
        Producer producer = producers.remove(producerId);
        if (producer == null) {
            return false;
        }

        Connection connection = getConnectionById(producer.getConnectionId());
        if (connection == null) {
            return false;
        }

        connection.removeProducer(producer.getTopic(), producer.getApp());
        eventManager.add(new SessionEvent(SessionEventType.RemoveProducer, producer));
        return true;
    }

    /**
     * 获取producer
     *
     * @param id 生产者ID
     * @return 匹配的生产者
     */
    public Producer getProducerById(final String id) {
        return this.producers.get(id);
    }

    /**
     * 添加事件监听器
     *
     * @param listener 监听器
     */
    public void addListener(final EventListener<SessionEvent> listener) {
        if (listener != null) {
            this.eventManager.addListener(listener);
        }
    }

    /**
     * 移除事件监听器
     *
     * @param listener 监听器
     */
    public void removeListener(final EventListener<SessionEvent> listener) {
        if (listener != null) {
            this.eventManager.removeListener(listener);
        }
    }

    /**
     * 关闭生产者连接
     *
     * @param topic 主题
     * @param app   应用
     */
    public void closeProducer(final String topic, final String app) {
        Set<String> connectionIds = new HashSet<String>(20);

        String connectionId;
        Connection connection;
        Transport transport;
        Producer producer;
        Map.Entry<String, Producer> entry;
        // 遍历生产者
        Iterator<Map.Entry<String, Producer>> it = producers.entrySet().iterator();
        while (it.hasNext()) {
            // 得到生产者
            entry = it.next();
            producer = entry.getValue();
            // 判断主题和应用
            if (producer.getTopic().equals(topic) && producer.getApp().equals(app)) {
                // 判断连接是否已经处理过
                connectionId = producer.getConnectionId();
                if (connectionIds.add(connectionId)) {
                    closeRelatedTransport(connectionId);
                }
            }
        }

    }

    /**
     * 关闭消费者连接
     *
     * @param topic 主题
     * @param app   应用
     */
    public void closeConsumer(final String topic, final String app) {
        Set<String> connectionIds = new HashSet<String>(20);

        Connection connection;
        Transport transport;
        String connectionId;
        Consumer consumer;
        Map.Entry<String, Consumer> entry;
        // 遍历消费者
        Iterator<Map.Entry<String, Consumer>> it = consumers.entrySet().iterator();
        while (it.hasNext()) {
            // 得到消费者
            entry = it.next();
            consumer = entry.getValue();
            // 判断主题和应用
            if ((topic == null || consumer.getTopic().equals(topic)) && (app == null || consumer.getApp().equals(app))) {
                // 判断连接是否已经处理过
                connectionId = consumer.getConnectionId();
                if (connectionIds.add(connectionId)) {
                    closeRelatedTransport(connectionId);

                }
            }
        }
    }

    private void closeRelatedTransport(String connectionId) {
        Connection connection;
        Transport transport;// 取到连接
        connection = connections.get(connectionId);
        if (connection != null) {
            // 关闭连接
            transport = connection.getTransport();
            if (transport != null) {
                // 关闭通道会广播事件，自动触发清理消费者和连接
                transport.stop();
            }
        }
    }

    /**
     * 事件类型
     */
    public enum SessionEventType {
        /**
         * 增加连接
         */
        AddConnection,
        /**
         * 删除连接
         */
        RemoveConnection,
        /**
         * 增加生产者
         */
        AddProducer,
        /**
         * 删除生产者
         */
        RemoveProducer,
        /**
         * 增加消费者
         */
        AddConsumer,
        /**
         * 增加消费者
         */
        RemoveConsumer,
    }

    /**
     * 会话事件
     */
    public static class SessionEvent {
        // 类型
        private SessionEventType type;
        // 连接
        private Connection connection;
        // 消费者
        private Consumer consumer;
        // 生产者
        private Producer producer;

        public SessionEvent(SessionEventType type, Connection connection) {
            this.type = type;
            this.connection = connection;
        }

        public SessionEvent(SessionEventType type, Consumer consumer) {
            this.type = type;
            this.consumer = consumer;
        }

        public SessionEvent(SessionEventType type, Producer producer) {
            this.type = type;
            this.producer = producer;
        }

        public SessionEventType getType() {
            return type;
        }

        public void setType(SessionEventType type) {
            this.type = type;
        }

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public Consumer getConsumer() {
            return consumer;
        }

        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
        }

        public Producer getProducer() {
            return producer;
        }

        public void setProducer(Producer producer) {
            this.producer = producer;
        }

        @Override
        public String toString() {
            return "SessionEvent{" +
                    "type=" + type +
                    ", connection=" + connection +
                    ", consumer=" + consumer +
                    ", producer=" + producer +
                    '}';
        }
    }

}