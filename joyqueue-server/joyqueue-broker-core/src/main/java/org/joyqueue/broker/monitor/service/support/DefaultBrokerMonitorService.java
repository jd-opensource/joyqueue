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
package org.joyqueue.broker.monitor.service.support;

import org.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import org.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata;
import org.joyqueue.broker.coordinator.group.domain.GroupMetadata;
import org.joyqueue.broker.monitor.service.ArchiveMonitorService;
import org.joyqueue.broker.monitor.service.BrokerMonitorInternalService;
import org.joyqueue.broker.monitor.service.BrokerMonitorService;
import org.joyqueue.broker.monitor.service.ConnectionMonitorService;
import org.joyqueue.broker.monitor.service.ConsumerMonitorService;
import org.joyqueue.broker.monitor.service.CoordinatorMonitorService;
import org.joyqueue.broker.monitor.service.MetadataMonitorService;
import org.joyqueue.broker.monitor.service.PartitionMonitorService;
import org.joyqueue.broker.monitor.service.ProducerMonitorService;
import org.joyqueue.broker.monitor.service.TopicMonitorService;
import org.joyqueue.broker.monitor.stat.BrokerStatExt;
import org.joyqueue.broker.monitor.stat.ElectionEventStat;
import org.joyqueue.broker.monitor.stat.JVMStat;
import org.joyqueue.broker.monitor.stat.ReplicaNodeStat;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.model.Pager;
import org.joyqueue.monitor.ArchiveMonitorInfo;
import org.joyqueue.monitor.BrokerMonitorInfo;
import org.joyqueue.monitor.BrokerStartupInfo;
import org.joyqueue.monitor.ConnectionMonitorDetailInfo;
import org.joyqueue.monitor.ConnectionMonitorInfo;
import org.joyqueue.monitor.ConsumerMonitorInfo;
import org.joyqueue.monitor.ConsumerPartitionGroupMonitorInfo;
import org.joyqueue.monitor.ConsumerPartitionMonitorInfo;
import org.joyqueue.monitor.PartitionGroupMonitorInfo;
import org.joyqueue.monitor.PartitionMonitorInfo;
import org.joyqueue.monitor.ProducerMonitorInfo;
import org.joyqueue.monitor.ProducerPartitionGroupMonitorInfo;
import org.joyqueue.monitor.ProducerPartitionMonitorInfo;
import org.joyqueue.monitor.TopicMonitorInfo;
import org.joyqueue.response.BooleanResponse;
import org.joyqueue.toolkit.vm.GCEventListener;

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
    public BrokerStartupInfo getStartInfo() {
        return brokerMonitorInternalService.getStartInfo();
    }

    @Override
    public void addGcEventListener(GCEventListener listener) {
        brokerMonitorInternalService.addGcEventListener(listener);
    }

    @Override
    public JVMStat getJVMState() {
        return brokerMonitorInternalService.getJVMState();
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
    public Map<String, Long> getSendBackLogNumByTopic() {
        return archiveMonitorService.getSendBackLogNumByTopic();
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
    public TopicConfig getTopicMetadata(String topic, boolean isCluster) {
        return metadataMonitorService.getTopicMetadata(topic, isCluster);
    }

    @Override
    public TopicConfig rebuildTopicMetadata(String topic) {
        return metadataMonitorService.rebuildTopicMetadata(topic);
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
    public ReplicaNodeStat getReplicaState(String topic, int partitionGroup) {
        return partitionMonitorService.getReplicaState(topic,partitionGroup);
    }


    @Override
    public ElectionEventStat getReplicaRecentElectionEvent(String topic, int partitionGroup) {
        return partitionMonitorService.getReplicaRecentElectionEvent(topic, partitionGroup);
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
    public Object syncMetadata(String source, String target, int interval, boolean onlyCompare) {
        return metadataMonitorService.syncMetadata(source, target, interval, onlyCompare);
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

    @Override
    public String getConfigMetadata(String key) {
        return metadataMonitorService.getConfigMetadata(key);
    }

    @Override
    public Map<String, String> getConfigsMetadata() {
        return metadataMonitorService.getConfigsMetadata();
    }

    @Override
    public String updateConfigMetadata(String key, String group, String value) {
        return metadataMonitorService.updateConfigMetadata(key, group, value);
    }

    @Override
    public String getMetadataCluster() {
        return metadataMonitorService.getMetadataCluster();
    }

    @Override
    public String addMetadataNode(String uri) {
        return metadataMonitorService.addMetadataNode(uri);
    }

    @Override
    public String removeMetadataNode(String uri) {
        return metadataMonitorService.removeMetadataNode(uri);
    }

    @Override
    public String updateMetadataNode(List<String> uris) {
        return metadataMonitorService.updateMetadataNode(uris);
    }

    @Override
    public String executeMetadataCommand(String command, List<String> args) {
        return metadataMonitorService.executeMetadataCommand(command, args);
    }
}