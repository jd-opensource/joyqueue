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
package org.joyqueue.broker.polling;

import com.google.common.base.Preconditions;
import com.jd.laf.extension.Converts;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.session.Joint;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.service.ServiceThread;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 长轮询管理
 * <p>
 * Created by chengzhiliang on 2018/8/16.
 */
public class LongPollingManager extends Service {
    public static final String LONG_POLLING_QUEUE_SIZE = "broker.consume.long_polling_queue_size";
    //TODO 设置一个合理的值
    public static final int MAX_LONG_POLLING_QUEUE_SIZE = 10000;

    protected static Logger logger = LoggerFactory.getLogger(LongPollingManager.class);
    // 长轮询请求queue
    protected Queue<LongPolling> longPollingQueue = new LinkedBlockingQueue<>();
    // 消费者长轮询数量
    protected ConcurrentMap<Joint, AtomicInteger> counter = new ConcurrentHashMap<>();
    // 消息获取。
    protected Consume consumeManager;
    // 会话管理器
    protected SessionManager sessionManager;
    // 异步处理。
    protected Thread guardThread = null;
    // 长轮询线程池
    protected ExecutorService executorService;
    // 集群管理器
    protected ClusterManager clusterManager;
    // 配置管理
    protected PropertySupplier propertySupplier;

    public LongPollingManager(SessionManager sessionManager,
                              ClusterManager clusterManager,
                              Consume consumeManager,
                              PropertySupplier propertySupplier) {
        Preconditions.checkArgument(sessionManager != null, "sessionManager can not be null");
        Preconditions.checkArgument(clusterManager != null, "clusterManager can not be null");
        Preconditions.checkArgument(consumeManager != null, "consumeManager can not be null");
        Preconditions.checkArgument(propertySupplier != null, "propertySupplier can not be null");

        this.sessionManager = sessionManager;
        this.clusterManager = clusterManager;
        this.consumeManager = consumeManager;
        this.propertySupplier = propertySupplier;
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("LongPolling"));
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        counter.clear();
        // 守护进程，每100毫秒执行一次
        guardThread = new Thread(new ServiceThread(this, 100) {
            @Override
            public boolean onException(Throwable e) {
                logger.error(e.getMessage(), e);
                return true;
            }

            @Override
            protected void execute() throws Exception {
                // 处理长轮询
                processHoldRequest();
            }
        }, "LongPolling-Thread");
        guardThread.start();
        logger.info("long polling manager is started");
    }

    @Override
    protected void doStop() {
        super.doStop();
        if (guardThread != null) {
            guardThread.interrupt();
        }
        counter.clear();
        logger.info("long pull manager is stopped");
    }

    /**
     * 获取消费者计数器
     *
     * @param consumer 消费者
     * @return 计数器
     */
    protected AtomicInteger getCount(Consumer consumer) {
        AtomicInteger count = counter.get(consumer.getJoint());
        if (count == null) {
            count = new AtomicInteger(0);
            AtomicInteger old = counter.putIfAbsent(consumer.getJoint(), count);
            if (old != null) {
                count = old;
            }
        }
        return count;
    }


    /**
     * 添加长轮询请求
     *
     * @return 成功标示
     */
    public boolean suspend(LongPolling longPolling) {
        logger.debug("longPolling info:[{}], longPollingQueueSize:[{}]", longPolling, longPollingQueue.size());

        Consumer consumer = longPolling.getConsumer();
        if (consumer == null || longPolling.getLongPollingTimeout() == 0 || !isStarted()) {
            return false;
        }
        String topic = consumer.getTopic();
        // 长轮询数量不能超过主题队列数量
        AtomicInteger count = getCount(consumer);
        List<Short> masterPartitionList = clusterManager.getLocalPartitions(TopicName.parse(topic));
        if (count.get() >= masterPartitionList.size()) {
            return false;
        }

        // 超过容量
        if (longPollingQueue.size() >= getLongPollingQueueSize()) {
            return false;
        }

        // 入队
        if (longPollingQueue.offer(longPolling)) {
            // 增加消费长轮询计数器
            count.incrementAndGet();
            return true;
        }
        return false;
    }

    // 获取长轮训队列大小
    private int getLongPollingQueueSize() {
        Property property = propertySupplier.getProperty(LONG_POLLING_QUEUE_SIZE);
        if (property == null) {
            return MAX_LONG_POLLING_QUEUE_SIZE;
        }
        return Converts.getInteger(property.getValue());
    }

    /**
     * 处理长轮询请求，检查是否过期，是否有数据了
     */
    protected void processHoldRequest() throws Exception {
        int size = longPollingQueue.size();
        for (int i = 0; i < size; i++) {
            if (!isStarted()) {
                return;
            }
            long currentTime = SystemClock.now();
            LongPolling longPolling = longPollingQueue.poll();
            Consumer consumer = longPolling.getConsumer();
            AtomicInteger count = counter.get(consumer.getJoint());
            // 得到当前消费者
            consumer = sessionManager.getConsumerById(consumer.getId());
            if (consumer == null) {
                // 消费者不存在了，则抛弃该长轮询
                if (count != null) {
                    // 减少计数器
                    count.decrementAndGet();
                }
                longPolling.getLongPollingCallback().onExpire(longPolling.getConsumer());
            } else if (longPolling.getExpire() <= currentTime) {
                if (count != null) {
                    // 减少计数器
                    count.decrementAndGet();
                }
                // 长轮询过期了
                longPolling.getLongPollingCallback().onExpire(consumer);
            } else if (consumeManager.hasFreePartition(consumer)) {
                // 有空闲队列
                executorService.execute(new PullMessageTask(longPolling));
            } else {
                // 没有数据，则继续等待
                longPollingQueue.offer(longPolling);
            }
        }

    }

    /**
     * 重新拉取消息
     */
    protected class PullMessageTask implements Runnable {
        private final LongPolling longPolling;

        public PullMessageTask(LongPolling longPolling) {
            this.longPolling = longPolling;
        }

        @Override
        public void run() {
            if (!isStarted()) {
                return;
            }
            Consumer consumer = longPolling.getConsumer();
            AtomicInteger count = counter.get(consumer.getJoint());
            PullResult pullResult = null;
            try {
                // 取数据
                pullResult = consumeManager.getMessage(consumer, longPolling.getCount(), longPolling.getAckTimeout());
                if (pullResult != null && pullResult.getBuffers().size() > 0) {
                    // 回调成功
                    longPolling.getLongPollingCallback().onSuccess(consumer, pullResult);
                    if (count != null) {
                        count.decrementAndGet();
                    }
                } else if (isStarted()) {
                    // 重入队列，等待再次轮询
                    longPollingQueue.offer(longPolling);
                }

                if (!pullResult.getCode().equals(JoyQueueCode.SUCCESS)) {
                    logger.error("getMessage error, code: {}, consumer: {}", pullResult.getCode(), consumer);
                }
            } catch (Throwable th) {
                try {
                    logger.error("long pull error.", th);
                    if (count != null) {
                        count.decrementAndGet();
                    }
                    longPolling.getLongPollingCallback().onException(consumer, th);
                } catch (Exception e) {
                    logger.error("ack long pull error.", e);
                }

            }
        }


    }

}
