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
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.ApplicationToken;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.TopicMsgFilter;
import org.joyqueue.model.query.QTopicMsgFilter;
import org.joyqueue.msg.filter.support.TopicMessageFilterSupport;
import org.joyqueue.repository.TopicMsgFilterRepository;
import org.joyqueue.service.ApplicationTokenService;
import org.joyqueue.service.BrokerService;
import org.joyqueue.service.TopicMsgFilterService;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
@Service("topicMsgFilterService")
public class TopicMsgFilterServiceImpl extends PageServiceSupport<TopicMsgFilter, QTopicMsgFilter, TopicMsgFilterRepository> implements TopicMsgFilterService {

    private static final int MAX_POOL_SIZE = 3;
    private static final int APPEND_MAX_SIZE = 100;
    private static final long MAX_FILE_SIZE = 104857600;
    private static final String URL_FORMAT = "oms:joyqueue://%s@%s/default";
    private static final String FILE_PATH_FORMAT = "%s_%s_%s.txt";
    /**
     * 没有消费到任何数据
     */
    private static final long DEFAULT_TIME_OUT = 30_000L;
    /**
     * 消费到数据但没命中filter时，超过{@param DEFAULT_FILTER_TIME_OUT}则追加文件
     */
    private static final long DEFAULT_FILTER_TIME_OUT = 10_000L;

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(0, MAX_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("topic-msg-filter-%d").setDaemon(true).build());

    private static final Logger logger = LoggerFactory.getLogger(TopicMsgFilterServiceImpl.class);

    @Autowired
    private ApplicationTokenService applicationTokenService;

    @Autowired
    private BrokerService brokerService;

    private TopicMessageFilterSupport topicMessageFilterSupport = new TopicMessageFilterSupport();

    @Override
    public void execute(TopicMsgFilter filter) throws Exception {
        List<ApplicationToken> appTokens = applicationTokenService.findByApp(filter.getApp());
        if (CollectionUtils.isNotEmpty(appTokens)) {
            filter.setToken(appTokens.get(0).getToken());
        }
        List<Broker> brokers = brokerService.findByTopic(filter.getTopic());
        if (CollectionUtils.isNotEmpty(brokers)) {
            Broker broker = brokers.get(0);
            filter.setBrokerAddr(broker.getIp() + ":" + broker.getPort());
        }
        if (THREAD_POOL_EXECUTOR.getActiveCount() < MAX_POOL_SIZE) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return consume(filter);
                } catch (IOException e) {
                    logger.error("Message filter cause error: {}", e.getMessage());
                    return null;
                }
            }, THREAD_POOL_EXECUTOR).whenCompleteAsync((result, throwable) -> {
                if (StringUtils.isNotBlank(result)) {
                    topicMessageFilterSupport.output(result);
                }
                TopicMsgFilter msgFilter = repository.nextOne(filter.getUserCode());
                if(msgFilter!=null) {
                    try {
                        execute(msgFilter);
                    } catch (Exception e) {
                        logger.error("topic message filter execute error: {}",e.getMessage());
                    }
                }
            });
        } else {
            logger.error("启动的线程数最大不能超过:" + MAX_POOL_SIZE);
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

    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error deleting message filter file : {}", e.getMessage());
        }
    }

    private String consume(TopicMsgFilter msgFilter) throws IOException {
        final String url = String.format(URL_FORMAT, msgFilter.getApp(), msgFilter.getBrokerAddr());
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, msgFilter.getToken());
        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(url, keyValue);
        ExtensionConsumer consumer = (ExtensionConsumer) messagingAccessPoint.createConsumer();
        consumer.bindQueue(msgFilter.getTopic());
        consumer.start();
        QueueMetaData metaData = consumer.getQueueMetaData(msgFilter.getTopic());
        Map<Integer, Long> indexMapper = Maps.newHashMap();
        String filePath = String.format(FILE_PATH_FORMAT, msgFilter.getUserCode(),msgFilter.getUserId(), SystemClock.now());
        File file = createFile(filePath);
        long clock = SystemClock.now();
        long filterClock = clock;
        int appendCount = 0;
        StringBuilder strBuilder = new StringBuilder();
        consume:
        while (true) {
            for (QueueMetaData.Partition partition : metaData.partitions()) {
                long index = indexMapper.computeIfAbsent(partition.partitionId(), k -> 0L);
                List<Message> messages = consumer.batchReceive((short) partition.partitionId(), index, 1000 * 10);
                for (Message message : messages) {
                    clock = SystemClock.now();
                    String content = new String(message.getData());
                    if (topicMessageFilterSupport.match(content,msgFilter.getFilter())) {
                        filterClock = SystemClock.now();
                        strBuilder.append(content).append('\n');
                        appendCount++;
                        // 每1w行追加一次
                        if (appendCount >= APPEND_MAX_SIZE) {
                            if (file != null && strBuilder.length() > 0) {
                                FileUtils.writeStringToFile(file, strBuilder.toString(), StandardCharsets.UTF_8, true);
                                appendCount = 0;
                                strBuilder.delete(0, strBuilder.length());
                                // 如果文件大小>100M,则停止消费
                                if (Files.size(file.toPath()) > MAX_FILE_SIZE) {
                                    break consume;
                                }
                            }
                        }
                    }
                    consumer.batchAck(messages.stream().map(Message::getMessageReceipt).collect(Collectors.toList()));
                    if (message.extensionHeader().isPresent()) {
                        index = message.extensionHeader().get().getOffset() + 1;
                        indexMapper.put(partition.partitionId(), index);
                    }
                }
            }
            // 如果一直有消费数据，但是一直没有命中filter,则超过filter超时时间且strBuilder不为空，追加文件
            if (SystemClock.now() - filterClock >= DEFAULT_FILTER_TIME_OUT) {
                if (file != null && strBuilder.length() > 0) {
                    FileUtils.writeStringToFile(file, strBuilder.toString(), StandardCharsets.UTF_8, true);
                    appendCount = 0;
                    strBuilder.delete(0, strBuilder.length());
                    // 如果文件大小>100M,则停止消费
                    if (Files.size(file.toPath()) > MAX_FILE_SIZE) {
                        break;
                    }
                }
                filterClock = SystemClock.now();
            }
            if (SystemClock.now() - clock >= DEFAULT_TIME_OUT) {
                if (file != null && strBuilder.length() > 0) {
                    FileUtils.writeStringToFile(file, strBuilder.toString(), StandardCharsets.UTF_8, true);
                }
                break;
            }
        }
        consumer.stop();
        msgFilter.setStatus(-1);
        repository.update(msgFilter);
        logger.info("message filter consume finished");
        return filePath;
    }


    @Override
    public PageResult<TopicMsgFilter> findTopicMsgFilters(QPageQuery<QTopicMsgFilter> query) {
        return repository.findTopicMsgFiltersByQuery(query);
    }
}
