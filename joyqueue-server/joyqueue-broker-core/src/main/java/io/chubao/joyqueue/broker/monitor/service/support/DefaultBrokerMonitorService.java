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
package io.chubao.joyqueue.broker.monitor.service.support;

import io.chubao.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import io.chubao.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata;
import io.chubao.joyqueue.broker.coordinator.group.domain.GroupMetadata;
import io.chubao.joyqueue.broker.monitor.service.ArchiveMonitorService;
import io.chubao.joyqueue.broker.monitor.service.BrokerMonitorInternalService;
import io.chubao.joyqueue.broker.monitor.service.BrokerMonitorService;
import io.chubao.joyqueue.broker.monitor.service.ConnectionMonitorService;
import io.chubao.joyqueue.broker.monitor.service.ConsumerMonitorService;
import io.chubao.joyqueue.broker.monitor.service.CoordinatorMonitorService;
import io.chubao.joyqueue.broker.monitor.service.MetadataMonitorService;
import io.chubao.joyqueue.broker.monitor.service.PartitionMonitorService;
import io.chubao.joyqueue.broker.monitor.service.ProducerMonitorService;
import io.chubao.joyqueue.broker.monitor.service.TopicMonitorService;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStatExt;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.model.Pager;
import io.chubao.joyqueue.monitor.ArchiveMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;
import io.chubao.joyqueue.monitor.ConnectionMonitorDetailInfo;
import io.chubao.joyqueue.monitor.ConnectionMonitorInfo;
import io.chubao.joyqueue.monitor.ConsumerMonitorInfo;
import io.chubao.joyqueue.monitor.ConsumerPartitionGroupMonitorInfo;
import io.chubao.joyqueue.monitor.ConsumerPartitionMonitorInfo;
import io.chubao.joyqueue.monitor.PartitionGroupMonitorInfo;
import io.chubao.joyqueue.monitor.PartitionMonitorInfo;
import io.chubao.joyqueue.monitor.ProducerMonitorInfo;
import io.chubao.joyqueue.monitor.ProducerPartitionGroupMonitorInfo;
import io.chubao.joyqueue.monitor.ProducerPartitionMonitorInfo;
import io.chubao.joyqueue.monitor.TopicMonitorInfo;
import io.chubao.joyqueue.response.BooleanResponse;

import java.util.List;
import java.util.Map;

/**
 * DefaultBrokerMonitorService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class DefaultBrokerMonitorService implements BrokerMonitorService {

    private BrokerMonitorInternalService brokerMonitorInternalService;
    private ConnectionMonitorService connectionMonitorService;
    private ConsumerMonitorService consumerMonitorService;
    private ProducerMonitorService producerMonitorService;
    private TopicMonitorService topicMonitorService;
    private PartitionMonitorService partitionMonitorService;
    private CoordinatorMonitorService coordinatorMonitorService;
    private ArchiveMonitorService archiveMonitorService;
    private MetadataMonitorService metadataMonitorService;

    public DefaultBrokerMonitorService(BrokerMonitorInternalService brokerMonitorInternalService, ConnectionMonitorService connectionMonitorService, ConsumerMonitorService consumerMonitorService,
                                       ProducerMonitorService producerMonitorService, TopicMonitorService topicMonitorService, PartitionMonitorService partitionMonitorService,
                                       CoordinatorMonitorService coordinatorMonitorService, ArchiveMonitorService archiveMonitorService, MetadataMonitorService metadataMonitorService) {
        this.brokerMonitorInternalService = brokerMonitorInternalService;
        this.connectionMonitorService = connectionMonitorService;
        this.consumerMonitorService = consumerMonitorService;
        this.producerMonitorService = producerMonitorService;
        this.topicMonitorService = topicMonitorService;
        this.partitionMonitorService = partitionMonitorService;
        this.coordinatorMonitorService = coordinatorMonitorService;
        this.archiveMonitorService = archiveMonitorService;
        this.metadataMonitorService = metadataMonitorService;
    }

    @Override
    public BrokerMonitorInfo getBrokerInfo() {
        return brokerMonitorInternalService.getBrokerInfo();
    }

    @Override
    public ConnectionMonitorInfo getConnectionInfo() {
        return connectionMonitorService.getConnectionInfo();
    }

    @Override
    public ConnectionMonitorInfo getConnectionInfoByTopic(String topic) {
        return connectionMonitorService.getConnectionInfoByTopic(topic);
    }

    @Override
    public ConnectionMonitorInfo getConnectionInfoByTopicAndApp(String topic, String app) {
        return connectionMonitorService.getConnectionInfoByTopicAndApp(topic, app);
    }

    @Override
    public ConnectionMonitorDetailInfo getConnectionDetailInfo() {
        return connectionMonitorService.getConnectionDetailInfo();
    }

    @Override
    public ConnectionMonitorDetailInfo getConnectionDetailInfoByTopic(String topic) {
        return connectionMonitorService.getConnectionDetailInfoByTopic(topic);
    }

    @Override
    public ConnectionMonitorDetailInfo getConnectionDetailInfoByTopicAndApp(String topic, String app) {
        return connectionMonitorService.getConnectionDetailInfoByTopicAndApp(topic, app);
    }

    @Override
    public ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopic(String topic) {
        return connectionMonitorService.getConsumerConnectionDetailInfoByTopic(topic);
    }

    @Override
    public ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopicAndApp(String topic, String app) {
        return connectionMonitorService.getConsumerConnectionDetailInfoByTopicAndApp(topic, app);
    }

    @Override
    public ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopic(String topic) {
        return connectionMonitorService.getProducerConnectionDetailInfoByTopic(topic);
    }

    @Override
    public ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopicAndApp(String topic, String app) {
        return connectionMonitorService.getProducerConnectionDetailInfoByTopicAndApp(topic, app);
    }

    @Override
    public Pager<ConsumerMonitorInfo> getConsumerInfos(int page, int pageSize) {
        return consumerMonitorService.getConsumerInfos(page, pageSize);
    }

    @Override
    public ConsumerMonitorInfo getConsumerInfoByTopicAndApp(String topic, String app) {
        return consumerMonitorService.getConsumerInfoByTopicAndApp(topic, app);
    }

    @Override
    public List<ConsumerPartitionMonitorInfo> getConsumerPartitionInfos(String topic, String app) {
        return consumerMonitorService.getConsumerPartitionInfos(topic, app);
    }

    @Override
    public ConsumerPartitionMonitorInfo getConsumerPartitionInfoByTopicAndApp(String topic, String app, short partition) {
        return consumerMonitorService.getConsumerPartitionInfoByTopicAndApp(topic, app, partition);
    }

    @Override
    public ConsumerPartitionGroupMonitorInfo getConsumerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId) {
        return consumerMonitorService.getConsumerPartitionGroupInfoByTopicAndApp(topic, app, partitionGroupId);
    }

    @Override
    public List<ConsumerPartitionGroupMonitorInfo> getConsumerPartitionGroupInfos(String topic, String app) {
        return consumerMonitorService.getConsumerPartitionGroupInfos(topic, app);
    }

    @Override
    public PartitionMonitorInfo getPartitionInfoByTopic(String topic, short partition) {
        return partitionMonitorService.getPartitionInfoByTopic(topic, partition);
    }

    @Override
    public List<PartitionMonitorInfo> getPartitionInfosByTopic(String topic) {
        return partitionMonitorService.getPartitionInfosByTopic(topic);
    }

    @Override
    public PartitionMonitorInfo getPartitionInfoByTopicAndApp(String topic, String app, short partition) {
        return partitionMonitorService.getPartitionInfoByTopicAndApp(topic, app, partition);
    }

    @Override
    public List<PartitionMonitorInfo> getPartitionInfosByTopicAndApp(String topic, String app) {
        return partitionMonitorService.getPartitionInfosByTopicAndApp(topic, app);
    }

    @Override
    public PartitionGroupMonitorInfo getPartitionGroupInfoByTopic(String topic, int partitionGroup) {
        return partitionMonitorService.getPartitionGroupInfoByTopic(topic, partitionGroup);
    }

    @Override
    public List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopic(String topic) {
        return partitionMonitorService.getPartitionGroupInfosByTopic(topic);
    }

    @Override
    public PartitionGroupMonitorInfo getPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroup) {
        return partitionMonitorService.getPartitionGroupInfoByTopicAndApp(topic, app, partitionGroup);
    }

    @Override
    public List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopicAndApp(String topic, String app) {
        return partitionMonitorService.getPartitionGroupInfosByTopicAndApp(topic, app);
    }

    @Override
    public Pager<ProducerMonitorInfo> getProduceInfos(int page, int pageSize) {
        return producerMonitorService.getProduceInfos(page, pageSize);
    }

    @Override
    public ProducerMonitorInfo getProducerInfoByTopicAndApp(String topic, String app) {
        return producerMonitorService.getProducerInfoByTopicAndApp(topic, app);
    }

    @Override
    public List<ProducerPartitionMonitorInfo> getProducerPartitionInfos(String topic, String app) {
        return producerMonitorService.getProducerPartitionInfos(topic, app);
    }

    @Override
    public ProducerPartitionMonitorInfo getProducerPartitionInfoByTopicAndApp(String topic, String app, short partition) {
        return producerMonitorService.getProducerPartitionInfoByTopicAndApp(topic, app, partition);
    }

    @Override
    public List<ProducerPartitionGroupMonitorInfo> getProducerPartitionGroupInfos(String topic, String app) {
        return producerMonitorService.getProducerPartitionGroupInfos(topic, app);
    }

    @Override
    public ProducerPartitionGroupMonitorInfo getProducerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId) {
        return producerMonitorService.getProducerPartitionGroupInfoByTopicAndApp(topic, app, partitionGroupId);
    }

    @Override
    public Pager<TopicMonitorInfo> getTopicInfos(int page, int pageSize) {
        return topicMonitorService.getTopicInfos(page, pageSize);
    }

    @Override
    public TopicMonitorInfo getTopicInfoByTopic(String topic) {
        return topicMonitorService.getTopicInfoByTopic(topic);
    }

    @Override
    public List<TopicMonitorInfo> getTopicInfoByTopics(List<String> topics) {
        return topicMonitorService.getTopicInfoByTopics(topics);
    }

    @Override
    public CoordinatorDetail getCoordinator(String groupId) {
        return coordinatorMonitorService.getCoordinator(groupId);
    }

    @Override
    public GroupMetadata getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat) {
        return coordinatorMonitorService.getCoordinatorGroup(namespace, groupId, topic, isFormat);
    }

    @Override
    public Map<String, GroupMemberMetadata> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat) {
        return coordinatorMonitorService.getCoordinatorGroupMembers(namespace, groupId, topic, isFormat);
    }

    @Override
    public long getConsumeBacklogNum() {
        return archiveMonitorService.getConsumeBacklogNum();
    }

    @Override
    public long getSendBackLogNum() {
        return archiveMonitorService.getSendBackLogNum();
    }

    @Override
    public ArchiveMonitorInfo getArchiveMonitorInfo() {
        return archiveMonitorService.getArchiveMonitorInfo();
    }

    @Override
    public BrokerStatExt getExtendBrokerStat(long timeStamp) {
        return brokerMonitorInternalService.getExtendBrokerStat(timeStamp);
    }

    @Override
    public BrokerStartupInfo getStartInfo() {
        return brokerMonitorInternalService.getStartInfo();
    }
    @Override
    public TopicConfig getTopicMetadata(String topic, boolean isCluster) {
        return metadataMonitorService.getTopicMetadata(topic, isCluster);
    }

    @Override
    public BooleanResponse getReadableResult(String topic, String app, String address) {
        return metadataMonitorService.getReadableResult(topic, app, address);
    }

    @Override
    public BooleanResponse getWritableResult(String topic, String app, String address) {
        return metadataMonitorService.getWritableResult(topic, app, address);
    }

    @Override
    public Consumer getConsumerMetadataByTopicAndApp(String topic, String app, boolean isCluster) {
        return metadataMonitorService.getConsumerMetadataByTopicAndApp(topic, app, isCluster);
    }

    @Override
    public Producer getProducerMetadataByTopicAndApp(String topic, String app, boolean isCluster) {
        return metadataMonitorService.getProducerMetadataByTopicAndApp(topic, app, isCluster);
    }

    @Override
    public Object exportMetadata(String source) {
        return metadataMonitorService.exportMetadata(source);
    }

    @Override
    public Object syncMetadata(String source, String target, int interval) {
        return metadataMonitorService.syncMetadata(source, target, interval);
    }

    @Override
    public Object queryMetadata(String source, String operator, List<Object> params) {
        return metadataMonitorService.queryMetadata(source, operator, params);
    }

    @Override
    public Object updateMetadata(String source, String operator, List<Object> params) {
        return metadataMonitorService.updateMetadata(source, operator, params);
    }

    @Override
    public Object insertMetadata(String source, String operator, List<Object> params) {
        return metadataMonitorService.insertMetadata(source, operator, params);
    }

    @Override
    public Object deleteMetadata(String source, String operator, List<Object> params) {
        return metadataMonitorService.deleteMetadata(source, operator, params);
    }
}