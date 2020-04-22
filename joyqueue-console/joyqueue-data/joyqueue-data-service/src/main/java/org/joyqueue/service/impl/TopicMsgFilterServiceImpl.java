/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.service.impl;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.joyqueue.consumer.ExtensionConsumer;
import io.openmessaging.message.Message;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.domain.TopicName;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.TopicMsgFilter;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.ApplicationToken;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.exception.NotFoundException;
import org.joyqueue.model.query.QTopicMsgFilter;
import org.joyqueue.monitor.PartitionAckMonitorInfo;
import org.joyqueue.msg.filter.support.TopicMessageFilterSupport;
import org.joyqueue.repository.TopicMsgFilterRepository;
import org.joyqueue.service.ApplicationTokenService;
import org.joyqueue.service.BrokerService;
import org.joyqueue.service.ConsumeOffsetService;
import org.joyqueue.service.ConsumerService;
import org.joyqueue.service.MessagePreviewService;
import org.joyqueue.service.TopicMsgFilterService;
import org.joyqueue.service.UserService;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
@Service("topicMsgFilterService")
public class TopicMsgFilterServiceImpl extends PageServiceSupport<TopicMsgFilter, QTopicMsgFilter, TopicMsgFilterRepository> implements TopicMsgFilterService {

    private final int maxPoolSize = 3;
    private final int appendMaxSize = 100;
    private final String urlFormat = "oms:joyqueue://%s@%s/default";
    private final String filePathFormat = "%s_%s_%s_%s_%s.txt";
    /**
     * 没有消费到任何数据
     */
    private final long defaultTimeOut = 30_000L;
    /**
     * 消费到数据但没命中filter时，超过{@param defaultFilterTimeOut}则追加文件
     */
    private final long defaultFilterTimeOut = 10_000L;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, maxPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("topic-msg-filter-%d").setDaemon(true).build());

    private static final Logger logger = LoggerFactory.getLogger(TopicMsgFilterServiceImpl.class);

    @Autowired
    private ApplicationTokenService applicationTokenService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private ConsumeOffsetService consumeOffsetService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessagePreviewService messagePreviewService;

    private final TopicMessageFilterSupport topicMessageFilterSupport = new TopicMessageFilterSupport();

    @Override
    public void execute() throws Exception {
        int rest = maxPoolSize - threadPoolExecutor.getActiveCount();
        if (rest > 0) {
            List<TopicMsgFilter> msgFilters = repository.findByNextOne(rest);
            if (CollectionUtils.isNotEmpty(msgFilters)) {
                for (TopicMsgFilter filter : msgFilters) {
                    try {
                        execute(filter);
                    } catch (NullPointerException | NotFoundException e) {
                        // status = -2, message query execute error
                        updateMsgFilterStatus(filter, -2, "", e.getMessage());
                        logger.error("topic message filter execute error: {}", e.getMessage());
                    } catch (Exception e) {
                        // reject by thread pool and reset status to 0 to execute later
                        updateMsgFilterStatus(filter, 0, "", "");
                        logger.error("topic message filter execute error: {}", e.getMessage());
                    }
                }
            } else {
                throw new IllegalAccessException("message filter queue has no task to execute");
            }
        } else {
            throw new IllegalAccessException("message filter task queue is full, please waiting until one of them finished");
        }
    }

    private void execute(TopicMsgFilter filter) throws Exception {
        List<String> apps;
        try {
            TopicName topicName = TopicName.parse(filter.getTopic());
            apps = consumerService.findByTopic(topicName.getCode(), topicName.getNamespace()).stream().map(consumer -> consumer.getApp().getCode()).collect(Collectors.toList());
        } catch (NullPointerException e) {
            throw new NotFoundException("topic not found or has unrelated app");
        }
        if (CollectionUtils.isNotEmpty(apps)) {
            updateMsgFilterStatus(filter, 1, null, "");
            filter.setApp(apps.get(0));
            List<ApplicationToken> appTokens = applicationTokenService.findByApp(filter.getApp());
            if (CollectionUtils.isNotEmpty(appTokens)) {
                filter.setToken(appTokens.get(0).getToken());
            } else {
                filter.setToken(createToken(apps.get(0)));
            }
            List<Broker> brokers = brokerService.findByTopic(filter.getTopic());
            if (CollectionUtils.isNotEmpty(brokers)) {
                Broker broker = brokers.get(0);
                filter.setBrokerAddr(broker.getIp() + ":" + broker.getPort());
            }
            CompletableFuture.supplyAsync(() -> {
                try {
                    return consume(filter);
                } catch (Exception e) {
                    logger.error("Message filter cause error: {}", e.getMessage());
                    // status = -2, message query execute error
                    updateMsgFilterStatus(filter, -2, "", e.getMessage());
                    return null;
                }
            }, threadPoolExecutor).whenComplete((result, throwable) -> {
                if (result != null) {
                    topicMessageFilterSupport.output(result.getId(), result.getCreateBy().getId(), result.getUrl());
                    try {
                        Files.deleteIfExists(Paths.get(result.getUrl()));
                        logger.info("Delete file: {} success", result.getUrl());
                    } catch (IOException e) {
                        logger.error("Failed to delete file: {}, need to delete manually", result.getUrl());
                    }
                }
            });
        } else {
            updateMsgFilterStatus(filter, -2, "", "topic not found or has unrelated app");
            throw new NotFoundException("topic not found or has unrelated app");
        }
    }

    private File createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                Files.delete(Paths.get(filePath));
            } catch (IOException e) {
                logger.error("Message filter file exists and failed to delete: {}", e.getMessage());
            }
        }
        try {
            if (file.createNewFile()) {
                logger.info("Create file named [{}]", file.getAbsolutePath());
                return file;
            }
        } catch (IOException e) {
            logger.error("Error creating message filter file: {}", e.getMessage());
        }
        return null;
    }

    private int appendFile(File file, StringBuilder strBuilder) throws IOException {
        if (file != null && strBuilder.length() > 0) {
            FileUtils.writeStringToFile(file, strBuilder.toString(), StandardCharsets.UTF_8, true);
            strBuilder.delete(0, strBuilder.length());
        }
        return 0;
    }

    private String buildFileHeader(TopicMsgFilter msgFilter) {
        StringBuilder builder = new StringBuilder();
        builder.append("Keyword: ").append(msgFilter.getFilter()).append('\n');
        builder.append("Topic: ").append(msgFilter.getTopic()).append('\n');
        builder.append("MessageFormat: ").append(msgFilter.getMsgFormat()).append('\n');
        if (msgFilter.getPartition() != null && msgFilter.getPartition() < 0) {
            builder.append("Partition: ").append("all partition").append('\n');
        } else {
            builder.append("Partition: ").append(msgFilter.getPartition()).append('\n');
        }
        if (msgFilter.getOffsetStartTime() != null) {
            builder.append("OffsetStartTime: ").append(msgFilter.getOffsetStartTime()).append('\n');
            builder.append("OffsetEndTime: ").append(msgFilter.getOffsetEndTime()).append('\n');
        } else {
            builder.append("Offset: ").append(msgFilter.getOffset()).append('\n');
            builder.append("QueryCount: ").append(msgFilter.getQueryCount()).append('\n');
        }
        builder.append("UserId: ").append(msgFilter.getCreateBy().getId()).append('\n');
        builder.append("UserCode: ").append(msgFilter.getCreateBy().getCode()).append('\n');
        builder.append("CreateTime: ").append(msgFilter.getCreateTime()).append('\n');
        builder.append("---------------------------------------------------------------------").append('\n');
        return builder.toString();
    }

    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error deleting message filter file : {}", e.getMessage());
        }
    }

    private String createToken(String app) throws Exception {
        ApplicationToken appToken = new ApplicationToken();
        appToken.setEffectiveTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        appToken.setApplication(new Identity(app));
        appToken.setExpirationTime(calendar.getTime());
        appToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
        applicationTokenService.add(appToken);
        return appToken.getToken();
    }

    private TopicMsgFilter consume(TopicMsgFilter msgFilter) throws IOException {
        final String url = String.format(urlFormat, msgFilter.getApp(), msgFilter.getBrokerAddr());
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, msgFilter.getToken());
        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(url, keyValue);
        ExtensionConsumer consumer = (ExtensionConsumer) messagingAccessPoint.createConsumer();
        consumer.bindQueue(msgFilter.getTopic());
        consumer.start();
        User user = userService.findById(msgFilter.getCreateBy().getId());
        msgFilter.getCreateBy().setCode(user.getCode());
        String filePath = String.format(filePathFormat, user.getCode(), msgFilter.getCreateBy().getId(),
                msgFilter.getTopic(), Thread.currentThread().getId(), SystemClock.now());
        File file = createFile(filePath);
        if (file != null) {
            FileUtils.writeStringToFile(file, buildFileHeader(msgFilter), StandardCharsets.UTF_8, true);
        }
        long clock = SystemClock.now();
        long filterClock = clock;
        int appendCount = 0;
        int appendTotalCount = 0;
        StringBuilder strBuilder = new StringBuilder();
        List<QueueMetaData.Partition> partitions = partitions(consumer, msgFilter);
        Map<Integer, Long> indexMapper = Maps.newHashMap();
        Map<Integer, Long> maxIndexMapper = Maps.newHashMap();
        if (msgFilter.getOffsetStartTime() != null && msgFilter.getOffsetEndTime() != null) {
            for (QueueMetaData.Partition partition : partitions) {
                parseOffsetByTs(msgFilter.getApp(), msgFilter.getTopic(), partition.partitionId(),
                        msgFilter.getOffsetStartTime().getTime(), msgFilter.getOffsetEndTime().getTime(),
                        indexMapper, maxIndexMapper);
            }
        } else {
            for (QueueMetaData.Partition partition : partitions) {
                indexMapper.put(partition.partitionId(), msgFilter.getOffset());
            }
        }
        while (msgFilter.getQueryCount() > appendTotalCount) {
            for (QueueMetaData.Partition partition : partitions) {
                long index = indexMapper.computeIfAbsent(partition.partitionId(), k -> 0L);
                List<Message> messages = consumer.batchReceive((short) partition.partitionId(), index, 1000 * 10);
                for (Message message : messages) {
                    clock = SystemClock.now();
                    String content = messagePreviewService.preview(msgFilter.getMsgFormat(), message.getData());
                    if (topicMessageFilterSupport.match(content, msgFilter.getFilter())) {
                        filterClock = SystemClock.now();
                        strBuilder.append(content).append('\n');
                        appendCount++;
                        appendTotalCount++;
                        // 每1w行追加一次
                        if (appendCount >= appendMaxSize) {
                            appendCount = appendFile(file, strBuilder);
                        }
                    }
                    if (message.extensionHeader().isPresent()) {
                        index = message.extensionHeader().get().getOffset() + 1;
                        indexMapper.put(partition.partitionId(), index);
                    }
                }
            }
            // 如果一直有消费数据，但是一直没有命中filter,则超过filter超时时间且strBuilder不为空，追加文件
            if (SystemClock.now() - filterClock >= defaultFilterTimeOut) {
                appendCount = appendFile(file, strBuilder);
                filterClock = SystemClock.now();
            }
            if (SystemClock.now() - clock >= defaultTimeOut) {
                appendFile(file, strBuilder);
                break;
            }
        }
        if (file != null && strBuilder.length() > 0) {
            FileUtils.writeStringToFile(file, strBuilder.toString(), StandardCharsets.UTF_8, true);
        }
        consumer.stop();
        // status = -1, 消息查询结束
        updateMsgFilterStatus(msgFilter, -1, "", "");
        msgFilter.setUrl(filePath);
        logger.info("message filter consume finished");
        return msgFilter;
    }

    private void updateMsgFilterStatus(TopicMsgFilter msgFilter, int status, String url, String description) {
        msgFilter.setStatus(status);
        if (StringUtils.isNoneBlank(url)) {
            msgFilter.setUrl(url);
        }
        if (StringUtils.isNoneBlank(description)) {
            msgFilter.setDescription(description);
        }
        msgFilter.setUpdateTime(new Date(SystemClock.now()));
        repository.update(msgFilter);
    }

    private void parseOffsetByTs(String app, String topicCode, int partition,
                                 long startTime, long endTime,
                                 Map<Integer, Long> indexMapper, Map<Integer, Long> maxIndexMapper) {
        Subscribe subscribe = new Subscribe();
        subscribe.setApp(new Identity(app));
        Topic topic = new Topic(topicCode);
        subscribe.setTopic(topic);
        subscribe.setNamespace(topic.getNamespace());
        subscribe.setType("消费者");
        parseOffset(subscribe, partition, startTime, indexMapper);
        parseOffset(subscribe, partition, endTime, maxIndexMapper);
    }

    private void parseOffset(Subscribe subscribe, int partition, long time, Map<Integer, Long> map) {
        List<PartitionAckMonitorInfo> partitionAckMonitorInfos = consumeOffsetService.timeOffset(subscribe, time);
        if (partition >= 0) {
            for (PartitionAckMonitorInfo monitorInfo : partitionAckMonitorInfos) {
                if (partition == monitorInfo.getPartition()) {
                    map.put(partition, monitorInfo.getIndex());
                }
            }
        }
    }

    /**
     * 如果用户指定了partition，则只消费指定的parititon，若没有指定，则消费所有的partition
     *
     * @param consumer
     * @param msgFilter
     * @return
     */
    private List<QueueMetaData.Partition> partitions(ExtensionConsumer consumer, TopicMsgFilter msgFilter) {
        QueueMetaData metaData = consumer.getQueueMetaData(msgFilter.getTopic());
        List<QueueMetaData.Partition> partitions = new ArrayList<>(1);
        if (msgFilter.getPartition() != null && msgFilter.getPartition() >= 0) {
            for (QueueMetaData.Partition partition : metaData.partitions()) {
                if (partition.partitionId() == msgFilter.getPartition()) {
                    partitions.add(partition);
                    break;
                }
            }
        } else {
            partitions = metaData.partitions();
        }
        return partitions;
    }


    @Override
    public PageResult<TopicMsgFilter> findTopicMsgFilters(QPageQuery<QTopicMsgFilter> query) {
        return repository.findTopicMsgFiltersByQuery(query);
    }
}
