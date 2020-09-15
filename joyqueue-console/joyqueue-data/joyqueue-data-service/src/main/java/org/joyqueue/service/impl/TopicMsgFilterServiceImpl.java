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
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.extension.ExtensionHeader;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.joyqueue.JoyQueueBuiltinKeys;
import io.openmessaging.joyqueue.consumer.ExtensionConsumer;
import io.openmessaging.message.Message;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.client.internal.consumer.exception.ConsumerException;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.Namespace;
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
import org.joyqueue.msg.filter.FilterResponse;
import org.joyqueue.msg.filter.OutputType;
import org.joyqueue.msg.filter.support.TopicMessageFilterSupport;
import org.joyqueue.repository.TopicMsgFilterRepository;
import org.joyqueue.service.ApplicationService;
import org.joyqueue.service.ApplicationTokenService;
import org.joyqueue.service.BrokerService;
import org.joyqueue.service.ConsumeOffsetService;
import org.joyqueue.service.ConsumerService;
import org.joyqueue.service.MessagePreviewService;
import org.joyqueue.service.TopicMsgFilterService;
import org.joyqueue.service.UserService;
import org.joyqueue.toolkit.network.IpUtil;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.Set;
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
    private final int appendMaxSize = 1000;
    private final int maxItemSize = 100 * 1000;
    private final String urlFormat = "oms:joyqueue://%s@%s/console";
    private final String filePathFormat = "%s_%s_%s_%s_%s.txt";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 没有消费到任何数据
     */
    private final long defaultTimeOut = 10 * 1000L;
    /**
     * 消费到数据但没命中filter时，超过{@param defaultFilterTimeOut}则追加文件
     */
    private final long defaultFilterTimeOut = 10 * 1000L;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, maxPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("topic-msg-filter-%d").setDaemon(true).build());

    private static final Logger logger = LoggerFactory.getLogger(TopicMsgFilterServiceImpl.class);

    @Autowired
    private ApplicationTokenService applicationTokenService;

    @Autowired
    private ApplicationService applicationService;

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
                    } catch (Exception e) {
                        // status = -2, message query execute error
                        updateMsgFilterStatus(filter, TopicMsgFilter.FilterStatus.ERROR, "", e.getMessage());
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

    private void enrichTopicMsgFilter(TopicMsgFilter filter, List<String> apps) throws Exception {
        if (!apps.contains(parseAppName(filter.getApp()))) {
            logger.error("app [{}] is not related with topic [{}]", filter.getApp(), filter.getTopic());
            throw new IllegalArgumentException("app ["+filter.getApp()+"] is not related with topic ["+filter.getTopic()+"]");
        }
        if (StringUtils.isNotBlank(filter.getToken())) {
            if (!validateToken(filter.getApp(), filter.getToken())) {
                logger.error("token is invalid, token: {}", filter.getToken());
                throw new IllegalArgumentException("token is invalid");
            }
        } else {
            logger.error("token is empty, filter: {}",filter);
            throw new IllegalArgumentException("token cannot be empty");
        }
        List<Broker> brokers = brokerService.findByTopic(filter.getTopic());
        if (CollectionUtils.isNotEmpty(brokers)) {
            Broker broker = brokers.get(0);
            filter.setBrokerAddr(broker.getIp() + ":" + broker.getPort());
        }
        User user = userService.findById(filter.getCreateBy().getId());
        filter.getCreateBy().setCode(user.getCode());
    }

    private String parseAppName(String app) {
        if (app.contains(".")) {
            return app.substring(0, app.indexOf('.'));
        }
        return app;
    }

    private boolean validateToken(String app, String token) {
        app = parseAppName(app);
        List<ApplicationToken> appTokens = applicationTokenService.findByApp(app);
        if (CollectionUtils.isNotEmpty(appTokens)) {
            Date date = new Date();
            List<ApplicationToken> collect = appTokens.stream()
                    .filter(appToken -> appToken.getToken().equals(token) && appToken.getExpirationTime().after(date)
                            && appToken.getEffectiveTime().before(date)).collect(Collectors.toList());
            return collect.size() > 0;
        }
        return false;
    }

    private void execute(TopicMsgFilter filter) throws Exception {
        List<String> apps;
        try {
            TopicName topicName = TopicName.parse(filter.getTopic());
            apps = consumerService.findByTopic(topicName.getCode(), topicName.getNamespace()).stream().map(consumer -> consumer.getApp().getCode()).collect(Collectors.toList());
        } catch (NullPointerException e) {
            logger.error("topic not found or doesn't have related app");
            throw new NotFoundException("topic not found or doesn't have related app", e);
        }
        if (CollectionUtils.isNotEmpty(apps)) {
            updateMsgFilterStatus(filter, TopicMsgFilter.FilterStatus.RUNNING, null, "");
            enrichTopicMsgFilter(filter, apps);
            CompletableFuture.supplyAsync(() -> {
                try {
                    return consume(filter);
                } catch (Exception e) {
                    logger.error("Message filter cause error", e);
                    // status = -2, message query execute error
                    updateMsgFilterStatus(filter, TopicMsgFilter.FilterStatus.ERROR, "", e.getMessage());
                    return null;
                }
            }, threadPoolExecutor).whenComplete((result, throwable) -> {
                if (result != null) {
                    updateMsgFilterStatus(result, TopicMsgFilter.FilterStatus.UPLOADING, "", "");
                    handleFilterOutputs(topicMessageFilterSupport.output(result.getUrl()), result);
                    updateMsgFilterStatus(result, TopicMsgFilter.FilterStatus.FINISHED, "", "");
                    deleteFile(result.getUrl());
                }
            });
        } else {
            updateMsgFilterStatus(filter, TopicMsgFilter.FilterStatus.ERROR, "", "topic not found or doesn't have related app");
            throw new NotFoundException("topic not found or doesn't have related app");
        }
    }

    private File initFile(TopicMsgFilter msgFilter) throws IOException {
        String filePath = String.format(filePathFormat, msgFilter.getCreateBy().getCode(), msgFilter.getCreateBy().getId(),
                msgFilter.getTopic(), Thread.currentThread().getId(), SystemClock.now());
        File file = createFile(filePath);
        if (file != null) {
            FileUtils.writeStringToFile(file, buildFileHeader(msgFilter), StandardCharsets.UTF_8, true);
        }
        return file;
    }

    private void deleteFile(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
            logger.info("Delete file: {} success", path);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}, need to delete manually", path);
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
        builder.append("App: ").append(msgFilter.getApp()).append('\n');
        builder.append("Topic: ").append(msgFilter.getTopic()).append('\n');
        builder.append("Token: ").append(msgFilter.getToken()).append('\n');
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
        builder.append("executeHost: ").append(IpUtil.getLocalIp()).append('\n');
        builder.append("CreateTime: ").append(msgFilter.getCreateTime()).append('\n');
        builder.append("---------------------------------------------------------------------").append('\n');
        return builder.toString();
    }

    private String createToken(String app) throws Exception {
        ApplicationToken appToken = new ApplicationToken();
        Date curDate = new Date();
        Date preDate = new Date(curDate.getTime() - 24*60*60*1000);
        appToken.setEffectiveTime(preDate);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Application application = applicationService.findByCode(app);
        appToken.setApplication(new Identity(application.getId(), app));
        appToken.setExpirationTime(calendar.getTime());
        appToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
        applicationTokenService.add(appToken);
        Thread.sleep(60_000L);
        return appToken.getToken();
    }

    private ExtensionConsumer initConsumer(TopicMsgFilter msgFilter) {
        final String url = String.format(urlFormat, msgFilter.getApp(), msgFilter.getBrokerAddr());
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, msgFilter.getToken());
        keyValue.put(JoyQueueBuiltinKeys.IO_THREADS, 1);
        keyValue.put(JoyQueueBuiltinKeys.BATCH_SIZE, 100);
        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(url, keyValue);
        ExtensionConsumer consumer = (ExtensionConsumer) messagingAccessPoint.createConsumer();
        consumer.bindQueue(msgFilter.getTopic());
        return consumer;
    }

    private TopicMsgFilter consume(TopicMsgFilter msgFilter) throws IOException {
        logger.info("filter: {}", msgFilter);
        ExtensionConsumer consumer = null;
        StringBuilder strBuilder = new StringBuilder();
        File file = initFile(msgFilter);
        try {
            consumer = initConsumer(msgFilter);
            consumer.start();
            long clock = SystemClock.now();
            long filterClock = clock;
            int appendCount = 0;
            int totalCount = 0;
            int hitCount = 0;
            boolean hasRest = true;
            List<QueueMetaData.Partition> partitions = partitions(consumer, msgFilter);
            Set<Integer> success  = new HashSet<>(0);
            Map<Integer, Long> indexMapper = Maps.newHashMap();
            Map<Integer, Long> maxIndexMapper = Maps.newHashMap();
            enrichIndexMap(msgFilter, partitions, indexMapper, maxIndexMapper);
            while (consumerCondition(msgFilter, totalCount, maxIndexMapper)) {
                for (QueueMetaData.Partition partition : partitions) {
                    if (success.contains(partition.partitionId())) {
                        continue;
                    }
                    if (msgFilter.getOffsetStartTime() != null && maxIndexMapper.size() > 0 && !maxIndexMapper.containsKey(partition.partitionId())) {
                        continue;
                    }
                    long index = indexMapper.computeIfAbsent(partition.partitionId(), k -> 0L);
                    List<Message> messages;
                    try {
                        messages = consumer.batchReceive((short) partition.partitionId(), index, 1000 * 10);
                    }catch(OMSRuntimeException e) {
                        if (e.getCause() instanceof ConsumerException) {
                            ConsumerException consumerException = (ConsumerException) e.getCause();
                            if (consumerException.getCode() == JoyQueueCode.FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE.getCode()) {
                                success.add(partition.partitionId());
                                logger.error("partition [{}] consume finished", partition.partitionId());
                            }
                        }
                        logger.warn("", e);
                        continue;
                    }
                    catch (Exception e) {
                        logger.error("Failed to receive message in partition [{}] with offset [{}], error: {}", partition.partitionId(), index, e);
                        continue;
                    }
                    for (Message message : messages) {
                        clock = SystemClock.now();
                        totalCount++;
                        String content = messagePreviewService.preview(msgFilter.getMsgFormat(), message.getData());
                        if (topicMessageFilterSupport.match(content, msgFilter.getFilter())) {
                            hitCount++;
                            filterClock = SystemClock.now();
                            strBuilder.append("partition:").append(partition.partitionId()).append(',');
                            if (message.extensionHeader().isPresent()) {
                                ExtensionHeader extensionHeader = message.extensionHeader().get();
                                if (extensionHeader.getMessageKey() != null) {
                                    strBuilder.append("messageKey:").append(extensionHeader.getMessageKey()).append(',');
                                }
                                strBuilder.append("offset:").append(message.extensionHeader().get().getOffset()).append(',');
                            }
                            strBuilder.append("bornTime:").append(dateFormat.format(new Date(message.header().getBornTimestamp()))).append('\n');
                            strBuilder.append(content).append('\n');
                            appendCount++;
                            // 每1w行追加一次
                            if (appendCount >= appendMaxSize) {
                                appendCount = appendFile(file, strBuilder);
                            }
                        }
                        if (message.extensionHeader().isPresent()) {
                            index = message.extensionHeader().get().getOffset() + 1;
                            indexMapper.put(partition.partitionId(), index);
                            if (maxIndexMapper.size() > 0 && maxIndexMapper.getOrDefault(partition.partitionId(), -2L) >= 0) {
                                // endTime < now()
                                if (indexMapper.get(partition.partitionId()) >= maxIndexMapper.get(partition.partitionId())) {
                                    maxIndexMapper.remove(partition.partitionId());
                                }
                            }
                        }
                    }
                }
                // 如果一直有消费数据，但是一直没有命中filter,则超过filter超时时间且strBuilder不为空，追加文件
                if (SystemClock.now() - filterClock >= defaultFilterTimeOut) {
                    appendCount = appendFile(file, strBuilder);
                    filterClock = SystemClock.now();
                }
                if (SystemClock.now() - clock >= defaultTimeOut) {
                    strBuilder.append("\n\n").append("consume finished, has no rest data.");
                    hasRest = false;
                    appendFile(file, strBuilder);
                    break;
                }
            }
            strBuilder.append("\n\n").append("TOTAL:").append(hitCount).append('\n');
            if (hasRest) {
                strBuilder.append("\n\n").append("consume finished, has rest data.");
            }
            // status = -1, 消息查询结束
            updateMsgFilterStatus(msgFilter, TopicMsgFilter.FilterStatus.FINISHED, "", "");
            logger.info("message filter consume finished.");
        } catch (Exception e) {
            updateMsgFilterStatus(msgFilter, TopicMsgFilter.FilterStatus.ERROR, "", e.getMessage());
            logger.info("message filter consume cause error and interrupted.");
            if (file != null) {
                Files.deleteIfExists(file.toPath());
            }
            throw e;
        } finally {
            if (file != null) {
                msgFilter.setUrl(file.getPath());
                if (strBuilder.length() > 0) {
                    FileUtils.writeStringToFile(file, strBuilder.toString(), StandardCharsets.UTF_8, true);
                }
            }
            if (consumer != null) {
                consumer.stop();
            }
        }
        return msgFilter;
    }

    private boolean consumerCondition(TopicMsgFilter msgFilter, int totalCount, Map<Integer, Long> maxIndexMap) {
        if (totalCount >= Math.max(msgFilter.getTotalCount(), maxItemSize)) {
            return false;
        }
        if (msgFilter.getOffsetStartTime() != null) {
            return maxIndexMap.size() > 0;
        } else {
            return msgFilter.getQueryCount() > totalCount;
        }
    }

    /**
     * @param msgFilter
     * @param status      -2：执行异常，-1：结束，0:等待，1：正在执行，2：正在上传
     * @param url
     * @param description
     */
    private void updateMsgFilterStatus(TopicMsgFilter msgFilter, TopicMsgFilter.FilterStatus status, String url, String description) {
        TopicMsgFilter updateMsgFilter = new TopicMsgFilter();
        updateMsgFilter.setId(msgFilter.getId());
        updateMsgFilter.setStatus(status.getStatus());
        if (StringUtils.isNoneBlank(url)) {
          updateMsgFilter.setUrl(url);
        }
        if (StringUtils.isNoneBlank(description)) {
            updateMsgFilter.setDescription(description);
        }
        updateMsgFilter.setUpdateTime(new Date(SystemClock.now()));
        repository.update(updateMsgFilter);
    }

    private void enrichIndexMap(TopicMsgFilter msgFilter, List<QueueMetaData.Partition> partitions, Map<Integer, Long> indexMapper, Map<Integer, Long> maxIndexMapper) {
        if (msgFilter.getOffsetStartTime() != null && msgFilter.getOffsetEndTime() != null) {
            parseOffsetByTs(msgFilter.getApp(), msgFilter.getTopic(), partitions.size() == 1 ? partitions.get(0).partitionId() : -1,
                    msgFilter.getOffsetStartTime().getTime(), msgFilter.getOffsetEndTime().getTime(),
                    indexMapper, maxIndexMapper);
        } else {
            for (QueueMetaData.Partition partition : partitions) {
                indexMapper.put(partition.partitionId(), msgFilter.getOffset());
            }
        }
    }

    private void parseOffsetByTs(String app, String topicCode, int partition,
                                 long startTime, long endTime,
                                 Map<Integer, Long> indexMapper, Map<Integer, Long> maxIndexMapper) {
        Subscribe subscribe = new Subscribe();
        if (app.contains(".")) {
            int idx = app.indexOf('.');
            subscribe.setSubscribeGroup(app.substring(idx));
            subscribe.setApp(new Identity(app.substring(0,idx)));
        } else {
            subscribe.setApp(new Identity(app));
        }
        TopicName topicName = TopicName.parse(topicCode);
        Topic topic = new Topic(topicName.getCode());
        Namespace namespace = new Namespace(topicName.getNamespace());
        subscribe.setTopic(topic);
        subscribe.setNamespace(namespace);
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
                    break;
                }
            }
        } else {
            for (PartitionAckMonitorInfo monitorInfo : partitionAckMonitorInfos) {
                map.put((int) monitorInfo.getPartition(), monitorInfo.getIndex());
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

    public void handleFilterOutputs(List<FilterResponse> responses, TopicMsgFilter msgFilter) {
        for (FilterResponse response : responses) {
            if (response.getType().equals(OutputType.S3)) {
                try {
                    TopicMsgFilter updateMsgFilter = new TopicMsgFilter();
                    updateMsgFilter.setUrl(response.getData().toString());
                    updateMsgFilter.setId(msgFilter.getId());
                    int conIdx = updateMsgFilter.getUrl().indexOf('?');
                    if (conIdx > 0) {
                        String url = updateMsgFilter.getUrl().substring(0, conIdx);
                        int lastIdx = url.lastIndexOf('/');
                        updateMsgFilter.setObjectKey(url.substring(lastIdx + 1));
                    }
                    updateMsgFilter.setUpdateTime(new Date(SystemClock.now()));
                    updateMsgFilter.setStatus(msgFilter.getStatus());
                    repository.update(updateMsgFilter);
                } catch (Exception e) {
                    logger.error("Failed to update url", e);
                }
            }
        }
    }

    @Override
    public PageResult<TopicMsgFilter> findTopicMsgFilters(QPageQuery<QTopicMsgFilter> query) {
        PageResult<TopicMsgFilter> pageResult = repository.findTopicMsgFiltersByQuery(query);
        for (int i=0;i<pageResult.getResult().size();i++) {
            Long id = pageResult.getResult().get(i).getCreateBy().getId();
            User user = userService.findById(id);
            pageResult.getResult().get(i).setCreateBy(new Identity(id, user.getCode()));
        }
        return pageResult;
    }
}
