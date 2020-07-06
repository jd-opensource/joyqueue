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
package org.joyqueue.server.retry.remote;

import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.server.retry.api.RetryPolicyProvider;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.server.retry.remote.command.GetRetry;
import org.joyqueue.server.retry.remote.command.GetRetryAck;
import org.joyqueue.server.retry.remote.command.GetRetryCount;
import org.joyqueue.server.retry.remote.command.GetRetryCountAck;
import org.joyqueue.server.retry.remote.command.PutRetry;
import org.joyqueue.server.retry.remote.command.UpdateRetry;
import org.joyqueue.server.retry.remote.config.RemoteRetryConfig;
import org.joyqueue.server.retry.remote.config.RemoteRetryConfigKey;
import org.joyqueue.toolkit.concurrent.LoopThread;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 远程重试管理器
 */
public class RemoteMessageRetry implements MessageRetry<Long> {
    private static final Logger logger = LoggerFactory.getLogger(RemoteMessageRetry.class);
    private PropertySupplier propertySupplier = null;
    // 远程调用限制的并发线程数
    private int remoteRetryLimitThread = (int ) RemoteRetryConfigKey.REMOTE_RETRY_LIMIT_THREADS.getValue();

    private long remoteRetryUpdateInterval = (long )RemoteRetryConfigKey.REMOTE_RETRY_UPDATE_INTERVAL.getValue();
    // 信号量（控制并发）
    private Semaphore retrySemaphore = null;
    // 连接通道客户端
    private TransportClient transportClient;
    // 可直连DB的broker的网络通道
    private RemoteTransportCollection transports;
    // 最大重试次数
    private int maxRetryTimes = 3;
    // start flag
    private boolean startFlag;
    // 远程重试扩展接口
    private RemoteRetryProvider remoteRetryProvider;
    // 重试主题策略
    private RetryPolicyProvider retryPolicyProvider;
    private RemoteRetryConfig config;


    public RemoteMessageRetry(RemoteRetryProvider remoteRetryProvider) {
        this.remoteRetryProvider = remoteRetryProvider;
    }

    @Override
    public void start() {
        retrySemaphore = new Semaphore(remoteRetryLimitThread);
        transportClient = remoteRetryProvider.createTransportClient();
        transports = new RemoteTransportCollection(BalanceType.ROLL, transportClient, remoteRetryUpdateInterval);

        startFlag = true;
        logger.info("remote retry manager is started");
    }

    @Override
    public boolean isStarted() {
        return startFlag;
    }

    @Override
    public void stop() {
        transports.stop();

        startFlag = false;
        logger.info("remote retry manager is stopped");
    }

    protected void checkStarted() throws JoyQueueException {
        if (!isStarted()) {
            throw new JoyQueueException(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE);
        }
    }

    @Override
    public void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider) {
        this.retryPolicyProvider = retryPolicyProvider;
    }

    @Override
    public void addRetry(List<RetryMessageModel> retryMessageModelList) throws JoyQueueException {
        if (CollectionUtils.isEmpty(retryMessageModelList)) {
            return;
        }
        checkStarted();

        PutRetry putRetryPayload = new PutRetry(retryMessageModelList);
        Command putRetryCommand = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.PUT_RETRY), putRetryPayload);
        Command sync = sync(putRetryCommand);
        if (sync.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode()) {
            throw new JoyQueueException(JoyQueueCode.RETRY_ADD, "");
        }
    }

    @Override
    public void retrySuccess(final String topic, final String app, final Long[] messageIds) throws JoyQueueException {
        checkStarted();
        remoteUpdateRetry(topic, app, messageIds, UpdateRetry.SUCCESS);
    }

    @Override
    public void retryError(final String topic, final String app, final Long[] messageIds) throws JoyQueueException {
        checkStarted();
        remoteUpdateRetry(topic, app, messageIds, UpdateRetry.FAILED);
    }

    @Override
    public void retryExpire(final String topic, final String app, final Long[] messageIds) throws JoyQueueException {
        checkStarted();
        remoteUpdateRetry(topic, app, messageIds, UpdateRetry.EXPIRED);
    }

    @Override
    public List<RetryMessageModel> getRetry(final String topic, final String app, short count, long startIndex) throws
            JoyQueueException {
        checkStarted();

        Semaphore semaphore = this.retrySemaphore;
        if (semaphore.tryAcquire()) {
            try {
                if (topic == null || topic.trim().isEmpty() || app == null || app.trim().isEmpty() || count <= 0) {
                    return new ArrayList<>();
                }

                GetRetry payload = new GetRetry().topic(topic).app(app).count(count).startId(startIndex);
                Command getRetryCommand = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.GET_RETRY), payload);

                Command ack = sync(getRetryCommand);
                if (ack.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode()) {
                    throw new JoyQueueException(JoyQueueCode.RETRY_GET, "");
                }


                if (ack != null) {
                    GetRetryAck ackPayload = (GetRetryAck) ack.getPayload();
                    List<RetryMessageModel> messageList = ackPayload.getMessages();
                    if (messageList != null && messageList.size() > 0) {
                        return messageList;
                    }
                }
            } catch (Exception e) {
                logger.error("getRetry exception, topic: {}, app: {}, index: {}, count: {}", topic, app, startIndex, count, e);
//                throw new JoyQueueException(JoyQueueCode.CN_REQUEST_ERROR, e); 远程重试失败，不抛异常，仅记录日志
            } finally {
                if (semaphore != null) {
                    semaphore.release();
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("tryAcquire failure:" + semaphore.drainPermits());
            }
        }

        return new ArrayList<>();
    }

    @Override
    public int countRetry(final String topic, final String app) throws JoyQueueException {
        checkStarted();

        try {
            if (topic == null || topic.trim().isEmpty() || app == null || app.trim().isEmpty()) {
                return 0;
            }

            GetRetryCount getRetryCountPayload = new GetRetryCount().topic(topic).app(app);
            Command getRetryCountCommand = new Command(
                    new JoyQueueHeader(Direction.REQUEST, CommandType.GET_RETRY_COUNT), getRetryCountPayload);
            Command ack = sync(getRetryCountCommand);

            if (ack.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode()) {
                logger.error("countRetry exception, topic: {}, app: {}, code: {}", topic, app, ack.getHeader().getStatus());
                return 0;
            }

            GetRetryCountAck payload = (GetRetryCountAck) ack.getPayload();
            return payload != null ? payload.getCount() : 0;
        } catch (Exception e) {
            logger.error("countRetry exception, topic: {}, app: {}", topic, app, e);
            return 0;
        }
    }

    /**
     * 远程更新重试
     *
     * @param topic    主题
     * @param app      应用
     * @param messages 消息数组
     * @param type     重试类型
     * @throws JoyQueueException
     */
    protected void remoteUpdateRetry(final String topic, final String app, final Long[] messages,
                                     final byte type) throws JoyQueueException {

        JoyQueueHeader header = new JoyQueueHeader(Direction.REQUEST, CommandType.UPDATE_RETRY);
        UpdateRetry updateRetryPayload = new UpdateRetry().topic(topic).app(app).messages(messages).updateType(type);
        Command updateRetryCommand = new Command(header, updateRetryPayload);

        Command sync = sync(updateRetryCommand);
        if (sync.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode()) {
            throw new JoyQueueException(JoyQueueCode.RETRY_UPDATE, "");
        }

    }


    /**
     * 重试发送命令
     *
     * @param request
     * @return
     * @throws JoyQueueException
     */
    protected Command sync(final Command request) throws JoyQueueException {
        Transport transport = transports.get();
        return transport.sync(request, config.getTransportTimeout());
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
        this.config = new RemoteRetryConfig(supplier);
        this.remoteRetryLimitThread = config.getLimitThreads();
        this.remoteRetryUpdateInterval = config.getUpdateInterval();
    }

    /**
     * 负载均衡类型
     */
    enum BalanceType {
        ROLL, Random
    }

    /**
     * 简单的远程通道集合
     * <br>
     * 1.轮询策略, 2.随机策略
     */
    class RemoteTransportCollection {

        private BalanceType balanceType;
        private TransportClient nettyClient;

        private Map<String, Transport> urlTransportMap = new HashMap<>();
        private CopyOnWriteArrayList<Transport> transportList = new CopyOnWriteArrayList<>();
        private AtomicInteger rollCounter = new AtomicInteger(0);
        private Random random = new Random();
        // 定时更新远程重试通道集合
        private final LoopThread updateThread;

        RemoteTransportCollection(BalanceType balanceType, TransportClient nettyClient, long updateInterval) {
            this.balanceType = balanceType;
            this.nettyClient = nettyClient;
            // 第一次构建远程重试服务通道集合
            buildRemoteTransport();
            // 启动定时更新线程
            this.updateThread = LoopThread.builder()
                    .sleepTime(updateInterval, updateInterval)
                    .name("Update-Remote-Transport-Thread")
                    .onException(e -> logger.error(e.getMessage(), e))
                    .doWork(this::buildRemoteTransport)
                    .build();
            updateThread.start();
        }

        /**
         * 创建远程连接
         */
        private void buildRemoteTransport() {
            logger.info("try to build remote transport.");

            Set<String> remoteUrls = getRemoteUrl();
            if (CollectionUtils.isEmpty(remoteUrls)) {
                logger.error("Remote retry url set is empty.");
                return;
            }
            // 移除无效的远程重试通道
            removeInvalid(remoteUrls);
            // 添加有效的远程重试通过
            addRemoteTransport(remoteUrls);

            logger.info("finish build remote transport.");
        }


        /**
         * 单个通道比较更新
         */
        private void addRemoteTransport(Set<String> urls) {
            if (CollectionUtils.isNotEmpty(urls)) {
                urls.stream().forEach(url -> {
                    Transport transport;
                    try {
                        // 不包含才创建并添加（防止重复创建、添加）
                        if (!urlTransportMap.containsKey(url)) {
                            transport = nettyClient.createTransport(url);
                            urlTransportMap.put(url, transport);
                            transportList.add(transport);

                            logger.info("add transport by url:[{}]", url);
                        }
                    } catch (Exception e) {
                        logger.error("create retry remote transport error." + e);
                    }
                });
            }

        }

        /**
         * 获取远程连接URL
         *
         * @return
         */
        private Set<String> getRemoteUrl() {
            return remoteRetryProvider.getUrls();
        }

        /**
         * 获取远程连接
         *
         * @return 远程连接通道
         */
        public Transport get() throws JoyQueueException {
            if (transportList.size() == 0) {
                throw new JoyQueueException("Have no transport error.", JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
            }
            if (balanceType == BalanceType.ROLL) {
                // 轮询策略
                int index = rollCounter.getAndIncrement();
                if (index < transportList.size()) {
                    return transportList.get(index);
                }
                if (rollCounter.compareAndSet(index + 1, 0)) {
                    return transportList.get(0);
                }
                return get();
            }
            // 随机策略
            int index = random.nextInt(transportList.size());
            return transportList.get(index);
        }

        /**
         * 移除通道并更新通道列表
         */
        public synchronized void removeInvalid(Set<String> urlSet) {
            Set<String> urls = urlTransportMap.keySet();
            if (CollectionUtils.isNotEmpty(urls)) {
                Iterator<String> iterator = urls.iterator();
                while (iterator.hasNext()) {
                    String url = iterator.next();
                    // 最新远程重试的URL集合不包含此URL，则移除
                    if (!urlSet.contains(url)) {
                        Transport remove = urlTransportMap.remove(url);
                        transportList.remove(remove);

                        logger.info("remove remote retry transport by url:[{}]", url);
                    }
                }
            }
        }

        /**
         * 销毁通道集合
         */
        public void stop() {
            transportList.stream().forEach(transport -> transport.stop());
        }

    }
}