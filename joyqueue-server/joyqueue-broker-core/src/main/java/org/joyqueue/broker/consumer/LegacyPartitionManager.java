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

import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.model.OwnerShip;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.retry.RetryProbability;
import org.joyqueue.domain.Consumer.ConsumerPolicy;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>分区管理</p>
 *
 * <li>分区占用管理</li>
 * <li>分区消费异常管理</li>
 * <br>
 * Created by chengzhiliang on 2018/8/17.
 */
@Deprecated
public class LegacyPartitionManager implements PartitionManager {

    private final Logger logger = LoggerFactory.getLogger(LegacyPartitionManager.class);

    // 集群管理器
    private ClusterManager clusterManager;
    // 会话管理
    private SessionManager sessionManager;
    // 分区->消费者
    private ConcurrentMap<ConsumePartition, OwnerShip> ownerShipCache = new ConcurrentHashMap<>();
    // 随机数字，用于选择重试队列
    private final Random random = new Random();
    // 重试概率
    private RetryProbability retryProbability = new RetryProbability();
    // 分区锁实例
    private PartitionLockInstance partitionLockInstance = new PartitionLockInstance();
    // 计数服务
    private CounterService counterService = new CounterService();

    public LegacyPartitionManager(ClusterManager clusterManager, SessionManager sessionManager) {
        this.clusterManager = clusterManager;
        this.sessionManager = sessionManager;

        // 添加会话断开后移除分区占用事件监听
        sessionManager.addListener(new RemoveOccupyListener());
    }

    /**
     * 尝试占用消费分区
     * <br>1.占用空闲分区
     * <br>2.释放过期占用
     *
     * @param consumer  消费者信息
     * @param partition 分区
     * @return 是否占用成功
     */
    @Override
    public boolean tryOccupyPartition(Consumer consumer, short partition, long occupyTimeout) {
        ConsumePartition consumePartition = new ConsumePartition(consumer.getTopic(), consumer.getApp(), partition);
        consumePartition.setConnectionId(consumer.getConnectionId());

        // 用消费者ID作为消费者唯一标示
        String clientId = consumer.getId();
        long expire = occupyTimeout + SystemClock.now();
        OwnerShip ownerShip = new OwnerShip(clientId, expire);
        // 是否成功占用
        boolean isSuccess = false;
        // 是否占用过多分区
        if (counterService.lockMorePartition(consumer)) {
            logger.info("Lock more partitions, consumer:{}", consumer);
            // 占用过多分区，直接返回false
            return false;
        }
        // 根据（主题+应用+分区）粒度进行同步锁，app用null表示
        ConsumePartition lockInstance = partitionLockInstance.getLockInstance(consumePartition);
        synchronized (lockInstance) {
            OwnerShip previous = ownerShipCache.get(consumePartition);
            if (previous != null) {
                // 是否过期，过期则占用成功，否则暂用失败
                if (previous.isExpire(SystemClock.now())) {
                    // 释放过期占用，新的占用覆盖过期占用
                    isSuccess = coverOccupy(consumePartition, previous, ownerShip);
                    // 由于过期，属于异常，所以记录上一个过期的消费者一次异常
                    increaseSerialErr(previous);

                    // 日志记录下占用过期分区和连接信息
                    logger.warn("expire occupy partition:[{}], connectionId:[{}]", consumePartition, consumer.getConnectionId());
                }
            } else {
                // 分区没有被占用，占用当前分区
                isSuccess = doOccupy(consumePartition, ownerShip);
            }
        }

        return isSuccess;
    }

    /**
     * 占用分区
     *
     * @param consumePartition 消费分区
     * @param ownerShip        占用者
     */
    private boolean doOccupy(ConsumePartition consumePartition, OwnerShip ownerShip) {
        ownerShipCache.put(consumePartition, ownerShip);
        counterService.increaseOccupyTimes(ownerShip.getOwner());// 将现在消费者的占用数量加上1
        return true;
    }

    /**
     * 占用分区（用于占用过期场景）
     *
     * @param consumePartition 消费分区
     * @param previous         以前的占用者
     * @param ownerShip        占用者
     */
    private boolean coverOccupy(ConsumePartition consumePartition, OwnerShip previous, OwnerShip ownerShip) {
        ownerShipCache.put(consumePartition, ownerShip);
        counterService.decreaseOccupyTimes(previous.getOwner()); // 将原来消费者的占用数量减去1
        counterService.increaseOccupyTimes(ownerShip.getOwner());// 将现在消费者的占用数量加上1
        return true;
    }

    /**
     * 释放占用
     *
     * @param consumePartition 消费分区
     * @return 是否释放成功
     */
    private boolean releaseOccupy(ConsumePartition consumePartition) {
        OwnerShip remove = ownerShipCache.remove(consumePartition);
        if (remove != null) {
            counterService.decreaseOccupyTimes(remove.getOwner());
        }
        return true;
    }

    /**
     * 释放占用的消费分区
     *
     * @param consumer  消费者信息
     * @param partition 消费分区
     * @return
     */
    @Override
    public boolean releasePartition(Consumer consumer, short partition) {
        ConsumePartition consumePartition = new ConsumePartition(consumer.getTopic(), consumer.getApp(), partition);
        return releasePartition(consumePartition);
    }

    /**
     * 释放占用的消费分区
     *
     * @param consumePartition 消费分区
     * @return
     */
    @Override
    public boolean releasePartition(ConsumePartition consumePartition) {
        return releaseOccupy(consumePartition);
    }

    /**
     * 是否需要暂停
     *
     * @param consumer 消费者信息
     * @return
     */
    @Override
    public boolean needPause(Consumer consumer) throws JoyQueueException {
        ConsumerPolicy consumerPolicy = clusterManager.getConsumerPolicy(TopicName.parse(consumer.getTopic()), consumer.getApp());
        Boolean isNeedPause = consumerPolicy.getPaused();
        return isNeedPause == null ? false : isNeedPause.booleanValue();
    }

    /**
     * 增加连续错误数量
     *
     * @param ownerShip 拥有者对象
     */
    @Override
    public void increaseSerialErr(OwnerShip ownerShip) {
        counterService.increaseErrTimes(ownerShip.getOwner());
    }

    /**
     * 重置连续出错数量
     *
     * @param consumer 消费者信息
     */
    @Override
    public void clearSerialErr(Consumer consumer) {
        counterService.clearErrTimes(consumer);
    }

    /**
     * 选择优先从哪个分区消费
     *
     * @param partitionSize  分区列表的大小
     * @param partitionIndex 分区再列表里的下标
     * @param accessTimes    消费主题的访问次数
     * @return
     */
    @Override
    public int selectPartitionIndex(int partitionSize, int partitionIndex, long accessTimes) {
        int index;
        if (partitionIndex < 0) {
            // 首次按照访问次数与分区个数取模
            index = (int) (accessTimes % partitionSize);
        } else {
            // 其他情况按照分区下标与分区个数取模
            index = partitionIndex % partitionSize;
        }

        return index;
    }

    /**
     * 是否重试
     *
     * @return 是否重试
     */
    @Override
    public boolean isRetry(Consumer consumer) throws JoyQueueException {

        int randomBound = clusterManager.getRetryRandomBound(consumer.getTopic(), consumer.getApp());
        if (randomBound <= 0) {
            return false;
        }

        Boolean retry = clusterManager.getConsumerPolicy(TopicName.parse(consumer.getTopic()), consumer.getApp()).getRetry();
        List<Short> masterPartitionList = clusterManager.getLocalPartitions(TopicName.parse(consumer.getTopic()));
        if (!retry.booleanValue() || !masterPartitionList.contains((short) 0)) {
            logger.debug("retry enable is false.");
            return false;
        }

        int val = random.nextInt(randomBound);
        // 重试管理中获取从重试分区消费的概率
        int rate = retryProbability.getProbability(consumer.getJoint());
        if (rate >= val) {
            return true;
        }
        return false;
    }

    /**
     * 重置最大概率
     *
     * @param maxProbability
     */
    @Override
    public void resetRetryProbability(Integer maxProbability) {
        retryProbability.resetMaxProbability(maxProbability);
    }

    /**
     * 增加读重试队列概率
     *
     * @param consumer
     */
    @Override
    public void increaseRetryProbability(Consumer consumer) {
        retryProbability.increase(consumer.getJoint());
    }

    /**
     * 减小读重试队列概率
     *
     * @param consumer
     */
    @Override
    public void decreaseRetryProbability(Consumer consumer) {
        retryProbability.decrease(consumer.getJoint());
    }


    /**
     * 获取高优先级的分区
     *
     * @param topic 消息主题
     * @return 高优先级分区集合
     */
    @Override
    public List<Short> getPriorityPartition(TopicName topic) {
        List<Short> priorityPartitionList = clusterManager.getPriorityPartitionList(topic);
        if (CollectionUtils.isEmpty(priorityPartitionList)) {
            priorityPartitionList = new ArrayList<>(0);
        }
        return priorityPartitionList;
    }

    /**
     * 根据分区反查丛属分组
     *
     * @param topic     消费主题
     * @param partition 消费分区
     * @return 分组编号
     */
    @Override
    public int getGroupByPartition(TopicName topic, short partition) {
        Integer partitionGroupId = clusterManager.getPartitionGroupId(topic, partition);
        if (partitionGroupId != null) {
            return partitionGroupId;
        } else {
            throw new IllegalArgumentException("Cannot find partitionGroup by topic:[" + topic + "],partition:[" + partition + "]");
        }
    }

    /**
     * 是否还有空闲分区
     *
     * @param consumer 消费者
     * @return 是否有空闲分区
     */
    @Override
    public boolean hasFreePartition(Consumer consumer) {
        boolean isFree = false;

        String clientId = consumer.getId();
        int occupyNum = counterService.getOccupyTimes(clientId);
        List<Short> masterPartitionList = clusterManager.getLocalPartitions(TopicName.parse(consumer.getTopic()));
        int partitionNum = masterPartitionList.size();
        if (partitionNum > occupyNum) {
            isFree = true;
        }

        return isFree;
    }

    /**
     * 计数服务
     * <br>
     * 用于分区占用过程中的异常计数、占用计数
     * <br>
     * todo 计数器可能存在内存泄漏风险，是否可采用事件驱动的方式，监听连接断开，移除消费者事件，然后清理相关计数器
     */
    private class CounterService {
        // 消费者->分区数，用与处理一个消费者占用了过多分区问题
        private ConcurrentMap</*clientId*/ String, Counter> occupyCounter = new ConcurrentHashMap<>();
        // 消费者->异常数，用于处理一个消费者占用了过多分区问题
        private ConcurrentMap</*clientId*/ String, Counter> errCounter = new ConcurrentHashMap<>();

        /**
         * 增加消费者的占用次数
         *
         * @param clientId
         */
        private void increaseOccupyTimes(String clientId) {
            Counter counter = occupyCounter.get(clientId);
            if (counter == null) {
                counter = new Counter();
                occupyCounter.put(clientId, counter);
            }
            counter.increase();
        }

        /**
         * 减少消费者的占用次数
         *
         * @param clientId
         */
        private void decreaseOccupyTimes(String clientId) {
            Counter counter = occupyCounter.get(clientId);
            if (counter == null) {
                return;
            }
            counter.decrease();
        }

        /**
         * 清零消费者的占用次数
         *
         * @param clientId
         */
        private void clearOccupyTimes(String clientId) {
            Counter counter = occupyCounter.get(clientId);
            if (counter == null) {
                return;
            }
            occupyCounter.remove(clientId);
        }

        /**
         * 一个客户端占用的分区数
         *
         * @param clientId
         * @return
         */
        private int getOccupyTimes(String clientId) {
            Counter counter = occupyCounter.get(clientId);
            if (counter == null) {
                return 0;
            }
            if (counter.isExpire()) {
                // 过期清零错误计数
                counter.clearTimes();
                return 0;
            }
            return counter.getTimes();
        }

        /**
         * 判断是否锁定过多的分区
         *
         * @return 是否锁定过多的分区
         */
        private boolean lockMorePartition(Consumer consumer) {
            String topic = consumer.getTopic();
            String app = consumer.getApp();
            int maxPartitionNum = 0;
            try {
                ConsumerPolicy consumerPolicy = clusterManager.getConsumerPolicy(TopicName.parse(topic), app);
                maxPartitionNum = consumerPolicy.getMaxPartitionNum();
            } catch (JoyQueueException e) {
                logger.error(e.getMessage(), e);
            }

            String clientId = consumer.getId();
            return getOccupyTimes(clientId) > maxPartitionNum;
        }

        /**
         * 获取错误计数
         *
         * @param consumer 消费者信息
         * @return
         */
        private int getErrTimes(Consumer consumer) {
            String clientId = consumer.getId();
            Counter counterObj = errCounter.get(clientId);
            if (counterObj == null) {
                return 0;
            }
            if (counterObj.isExpire()) {
                // 过期清零错误计数
                counterObj.clearTimes();
                return 0;
            }
            return counterObj.getTimes();
        }

        /**
         * 递增错误次数
         */
        private void increaseErrTimes(String clientId) {
            Counter counterObj = errCounter.get(clientId);
            if (counterObj == null) {
                counterObj = new Counter();
                // 放入错误计数器
                errCounter.put(clientId, counterObj);
            }
            counterObj.increase();
        }

        /**
         * 移除指定消费者的异常计数
         *
         * @param consumer 消费者信息
         */
        private void clearErrTimes(Consumer consumer) {
            String clientId = consumer.getId();
            errCounter.remove(clientId);
        }

        /**
         * 计数器对象
         */
        private class Counter {
            // 错误次数
            AtomicInteger times = new AtomicInteger(0);
            // 创建时间
            final long createTime = SystemClock.now();
            // 最新一个错误的时间
            volatile long updateTime = SystemClock.now();

            /**
             * 递增 & 更新时间
             */
            void increase() {
                times.incrementAndGet();
                updateTime = SystemClock.now();
            }

            /**
             * 递减 & 更新时间
             */
            int decrease() {
                updateTime = SystemClock.now();
                return times.decrementAndGet();
            }

            /**
             * 是否过期，默认过期时间1分钟
             */
            boolean isExpire() {
                return SystemClock.now() - updateTime > 60 * 1000;
            }

            /**
             * 清零计数
             */
            void clearTimes() {
                times.set(0);
            }

            /**
             * 获取连续出错次数
             */
            int getTimes() {
                return times.get();
            }

        }
    }

    /**
     * 监听回话断开时间，并移除被回话占用的分区占用
     */
    class RemoveOccupyListener implements EventListener<SessionManager.SessionEvent> {

        @Override
        public void onEvent(SessionManager.SessionEvent event) {
            if (event.getType() == SessionManager.SessionEventType.RemoveConsumer) {
                logger.info("Listen SessionManager.SessionEventType.RemoveConsumer, Event:[{}]", event);

                Consumer consumer = event.getConsumer();
                removeOccupyByConsumer(consumer);
            }
        }

        /**
         * 移除占用
         */
        private void removeOccupyByConsumer(Consumer consumer) {
            List<Short> masterPartitionList = clusterManager.getLocalPartitions(TopicName.parse(consumer.getTopic()));
            final String clientId = consumer.getId();
            masterPartitionList.stream().forEach(partition -> {
                ConsumePartition consumePartition = new ConsumePartition(consumer.getTopic(), consumer.getApp(), partition);
                OwnerShip ownerShip = ownerShipCache.get(consumePartition);

                // 判断是否同一个消费者
                if (ownerShip != null && StringUtils.equals(ownerShip.getOwner(), clientId)) {
                    logger.info("remove occupy by topic:[{}], app:[{}], partition:[{}]", consumer.getTopic(), consumer.getApp(), partition);
                    // 解除占用
                    ownerShipCache.remove(consumePartition);
                    // 清零该消费者的占用次数
                    counterService.clearOccupyTimes(clientId);
                    // 清零该消费者的出错次数
                    counterService.clearErrTimes(consumer);
                }
            });

        }
    }


}
