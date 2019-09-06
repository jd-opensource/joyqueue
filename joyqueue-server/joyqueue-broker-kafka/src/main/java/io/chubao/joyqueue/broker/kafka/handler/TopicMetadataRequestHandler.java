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
package io.chubao.joyqueue.broker.kafka.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.KafkaErrorCode;
import io.chubao.joyqueue.broker.kafka.command.TopicMetadataRequest;
import io.chubao.joyqueue.broker.kafka.command.TopicMetadataResponse;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.helper.KafkaClientHelper;
import io.chubao.joyqueue.broker.kafka.model.KafkaBroker;
import io.chubao.joyqueue.broker.kafka.model.KafkaPartitionMetadata;
import io.chubao.joyqueue.broker.kafka.model.KafkaTopicMetadata;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Partition;
import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.toolkit.delay.AbstractDelayedOperation;
import io.chubao.joyqueue.toolkit.delay.DelayedOperationKey;
import io.chubao.joyqueue.toolkit.delay.DelayedOperationManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TopicMetadataRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class TopicMetadataRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(TopicMetadataRequestHandler.class);

    private NameService nameService;
    private KafkaConfig config;
    private DelayedOperationManager delayPurgatory;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.nameService = kafkaContext.getBrokerContext().getNameService();
        this.config = kafkaContext.getConfig();
        this.delayPurgatory = new DelayedOperationManager("kafka-metadata-delayed");
        this.delayPurgatory.start();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        TopicMetadataRequest topicMetadataRequest = (TopicMetadataRequest) command.getPayload();
        String clientId = KafkaClientHelper.parseClient(topicMetadataRequest.getClientId());

        Map<String, TopicConfig> topicConfigs = null;
        if (CollectionUtils.isEmpty(topicMetadataRequest.getTopics()) && StringUtils.isNotBlank(clientId)) {
            topicConfigs = getAllTopicConfigs(clientId);
        } else {
            topicConfigs = getTopicConfigs(topicMetadataRequest.getTopics());
        }

        List<KafkaBroker> brokers = getTopicBrokers(topicConfigs);
        List<KafkaTopicMetadata> topicMetadata = getTopicMetadata(topicMetadataRequest.getTopics(), topicConfigs);

        TopicMetadataResponse topicMetadataResponse = new TopicMetadataResponse(topicMetadata, brokers);
        Command response = new Command(topicMetadataResponse);

        if (config.getLogDetail(clientId)) {
            logger.info("get topic metadata, transport: {}, app: {}, request: {}, response: {}",
                    transport, clientId, topicMetadataRequest, topicMetadataResponse);
        }

        if (config.getMetadataDelay() && CollectionUtils.isEmpty(topicMetadata)) {
            logger.info("get topic metadata, topics: {}, address: {}, metadata: {}, app: {}",
                    topicMetadataRequest.getTopics(), transport.remoteAddress(), JSON.toJSONString(topicMetadata), topicMetadataRequest.getClientId());

            delayPurgatory.tryCompleteElseWatch(new AbstractDelayedOperation(1000 * 1) {
                @Override
                protected void onComplete() {
                    transport.acknowledge(command, response);
                }
            }, Sets.newHashSet(new DelayedOperationKey()));
            return null;
        }

        return response;
    }

    @Override
    public int type() {
        return KafkaCommandType.METADATA.getCode();
    }

    protected Map<String, TopicConfig> getAllTopicConfigs(String clientId) {
        // TODO 常量
        String[] appGroup = clientId.split("\\.");
        Map<TopicName, TopicConfig> consumers = nameService.getTopicConfigByApp(clientId, Subscription.Type.CONSUMPTION);
        Map<TopicName, TopicConfig> producers = nameService.getTopicConfigByApp(appGroup[0], Subscription.Type.PRODUCTION);

        Map<String, TopicConfig> result = Maps.newHashMap();

        if (MapUtils.isNotEmpty(consumers)) {
            for (Map.Entry<TopicName, TopicConfig> entry : consumers.entrySet()) {
                result.put(entry.getKey().getFullName(), entry.getValue());
            }
        }
        if (MapUtils.isNotEmpty(producers)) {
            for (Map.Entry<TopicName, TopicConfig> entry : producers.entrySet()) {
                result.put(entry.getKey().getFullName(), entry.getValue());
            }
        }

        return result;
    }

    protected Map<String, TopicConfig> getTopicConfigs(List<String> topics) {
        Map<String, TopicConfig> result = Maps.newHashMap();
        for (String topic : topics) {
            TopicConfig topicConfig = nameService.getTopicConfig(TopicName.parse(topic));
            if (topicConfig != null) {
                result.put(topic, topicConfig);
            }
        }
        return result;
    }

    protected List<KafkaBroker> getTopicBrokers(Map<String, TopicConfig> topicConfigs) {
        Set<KafkaBroker> result = Sets.newHashSet();
        for (Map.Entry<String, TopicConfig> topicEntry : topicConfigs.entrySet()) {
            for (Map.Entry<Integer, Broker> entry : topicEntry.getValue().fetchAllBroker().entrySet()) {
                Broker broker = entry.getValue();
                KafkaBroker kafkaBroker = new KafkaBroker(broker.getId(), broker.getIp(), broker.getPort());
                result.add(kafkaBroker);
            }
        }
        return Lists.newArrayList(result);
    }

    protected List<KafkaTopicMetadata> getTopicMetadata(List<String> topics, Map<String, TopicConfig> topicConfigs) {
        List<KafkaTopicMetadata> result = Lists.newLinkedList();

        if (CollectionUtils.isEmpty(topics)) {
            for (Map.Entry<String, TopicConfig> entry : topicConfigs.entrySet()) {
                List<KafkaPartitionMetadata> kafkaPartitionMetadata = getPartitionMetadata(entry.getValue());
                KafkaTopicMetadata kafkaTopicMetadata = new KafkaTopicMetadata(entry.getKey(), kafkaPartitionMetadata, KafkaErrorCode.NONE.getCode());
                result.add(kafkaTopicMetadata);
            }
        } else {
            for (String topic : topics) {
                TopicConfig topicConfig = topicConfigs.get(topic);

                if (topicConfig != null) {
                    List<KafkaPartitionMetadata> kafkaPartitionMetadata = getPartitionMetadata(topicConfig);
                    KafkaTopicMetadata kafkaTopicMetadata = new KafkaTopicMetadata(topic, kafkaPartitionMetadata, KafkaErrorCode.NONE.getCode());
                    result.add(kafkaTopicMetadata);
                } else {
                    KafkaTopicMetadata kafkaTopicMetadata = new KafkaTopicMetadata(topic, Collections.emptyList(), KafkaErrorCode.TOPIC_AUTHORIZATION_FAILED.getCode());
                    result.add(kafkaTopicMetadata);
                }
            }
        }
        return result;
    }

    protected List<KafkaPartitionMetadata> getPartitionMetadata(TopicConfig topicConfig) {
        List<KafkaPartitionMetadata> result = Lists.newLinkedList();
        for (Partition partition : topicConfig.fetchPartitionMetadata()) {
            short errorCode = KafkaErrorCode.NONE.getCode();
            KafkaBroker leader = null;
            List<KafkaBroker> replicas = Lists.newLinkedList();
            List<KafkaBroker> isrs = Lists.newLinkedList();

            if (partition.getLeader() != null) {
                leader = new KafkaBroker(partition.getLeader().getId(), partition.getLeader().getIp(), partition.getLeader().getPort());
            } else {
                errorCode = KafkaErrorCode.LEADER_NOT_AVAILABLE.getCode();
            }

            for (Broker replica : partition.getReplicas()) {
                replicas.add(new KafkaBroker(replica.getId(), replica.getIp(), replica.getPort()));
            }

            for (Broker isr : partition.getIsrs()) {
                isrs.add(new KafkaBroker(isr.getId(), isr.getIp(), isr.getPort()));
            }

            KafkaPartitionMetadata kafkaPartitionMetadata = new KafkaPartitionMetadata(partition.getPartitionId(), leader, replicas, isrs, errorCode);
            result.add(kafkaPartitionMetadata);
        }
        return result;
    }
}