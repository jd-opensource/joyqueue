/**
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
package com.jd.journalq.broker.manage.service.support;

import com.jd.journalq.broker.manage.service.BrokerManageService;
import com.jd.journalq.broker.manage.service.ConnectionManageService;
import com.jd.journalq.broker.manage.service.ConsumerManageService;
import com.jd.journalq.broker.manage.service.CoordinatorManageService;
import com.jd.journalq.broker.manage.service.ElectionManageService;
import com.jd.journalq.broker.manage.service.MessageManageService;
import com.jd.journalq.broker.manage.service.StoreManageService;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.manage.IndexItem;
import com.jd.journalq.manage.PartitionGroupMetric;
import com.jd.journalq.manage.PartitionMetric;
import com.jd.journalq.manage.TopicMetric;
import com.jd.journalq.monitor.BrokerMessageInfo;
import com.jd.journalq.monitor.PartitionAckMonitorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/18
 */
public class DefaultBrokerManageService implements BrokerManageService {
    private static Logger logger = LoggerFactory.getLogger(DefaultBrokerManageService.class);

    private ConnectionManageService connectionManageService;
    private MessageManageService messageManageService;
    private StoreManageService storeManageService;
    private ConsumerManageService consumerManageService;
    private CoordinatorManageService coordinatorManageService;
    private ElectionManageService electionManageService;

    public DefaultBrokerManageService(ConnectionManageService connectionManageService, MessageManageService messageManageService,
                                      StoreManageService storeManageService, ConsumerManageService consumerManageService,
                                      CoordinatorManageService coordinatorManageService, ElectionManageService electionManageService) {
        this.connectionManageService = connectionManageService;
        this.messageManageService = messageManageService;
        this.storeManageService = storeManageService;
        this.consumerManageService = consumerManageService;
        this.coordinatorManageService = coordinatorManageService;
        this.electionManageService = electionManageService;
    }

    @Override
    public int closeProducer(String topic, String app) {
        return connectionManageService.closeProducer(topic, app);
    }

    @Override
    public int closeConsumer(String topic, String app) {
        return connectionManageService.closeConsumer(topic, app);
    }

    @Override
    public boolean setAckIndex(String topic, String app, short partition, long index) throws JournalqException {
        return consumerManageService.setAckIndex(topic, app, partition, index);
    }

    @Override
    public boolean setMaxAckIndex(String topic, String app, short partition) throws JournalqException {
        return consumerManageService.setMaxAckIndex(topic, app, partition);
    }

    @Override
    public long getAckIndex(String topic, String app, short partition) {
        return consumerManageService.getAckIndex(topic, app, partition);
    }

    @Override
    public List<PartitionAckMonitorInfo> getAckIndexes(String topic, String app) {
        return consumerManageService.getAckIndexes(topic, app);
    }

    @Override
    public boolean setMaxAckIndexes(String topic, String app) throws JournalqException {
        return consumerManageService.setMaxAckIndexes(topic, app);
    }

    @Override
    public boolean setAckIndexByTime(String topic, String app, short partition, long timestamp) throws JournalqException {
        return consumerManageService.setAckIndexByTime(topic, app, partition, timestamp);
    }

    @Override
    public boolean setAckIndexesByTime(String topic, String app, long timestamp) throws JournalqException {
        return consumerManageService.setAckIndexesByTime(topic, app, timestamp);
    }

    @Override
    public long getAckIndexByTime(String topic, String app, short partition, long timestamp) {
        return consumerManageService.getAckIndexByTime(topic, app, partition, timestamp);
    }

    @Override
    public List<PartitionAckMonitorInfo> getTopicAckIndexByTime(String topic, String app, long timestamp) {
        return consumerManageService.getTopicAckIndexByTime(topic,app,timestamp);
    }

    @Override
    public List<BrokerMessageInfo> getPartitionMessage(String topic, String app, short partition, long index, int count) {
        return messageManageService.getPartitionMessage(topic, app, partition, index, count);
    }

    @Override
    public List<BrokerMessageInfo> getPendingMessage(String topic, String app, int count) {
        return messageManageService.getPendingMessage(topic, app, count);
    }

    @Override
    public List<BrokerMessageInfo> getLastMessage(String topic, String app, int count) {
        return messageManageService.getLastMessage(topic, app, count);
    }

    @Override
    public List<BrokerMessageInfo> viewMessage(String topic, String app, int count) {
        return messageManageService.viewMessage(topic, app, count);
    }

    @Override
    public TopicMetric[] topicMetrics() {
        return storeManageService.topicMetrics();
    }

    @Override
    public TopicMetric topicMetric(String topic) {
        return storeManageService.topicMetric(topic);
    }

    @Override
    public PartitionGroupMetric partitionGroupMetric(String topic, int partitionGroup) {
        return storeManageService.partitionGroupMetric(topic, partitionGroup);
    }

    @Override
    public PartitionMetric partitionMetric(String topic, short partition) {
        return storeManageService.partitionMetric(topic, partition);
    }

    @Override
    public File[] listFiles(String path) {
        return storeManageService.listFiles(path);
    }

    @Override
    public File[] listAbsolutePathFiles(String path) {
        return storeManageService.listAbsolutePathFiles(path);
    }

    @Override
    public void removeTopic(String topic) {
        storeManageService.removeTopic(topic);
    }
    @Override
    public List<String> topics() {
        return storeManageService.topics();
    }

    @Override
    public List<PartitionGroupMetric> partitionGroups(String topic) {
        return storeManageService.partitionGroups(topic);
    }

    @Override
    public List<String> readPartitionGroupMessage(String topic, int partitionGroup, long position, int count) {
        return storeManageService.readPartitionGroupMessage(topic, partitionGroup, position, count);
    }

    @Override
    public List<String> readPartitionMessage(String topic, short partition, long index, int count) {
        return storeManageService.readPartitionMessage(topic, partition, index, count);
    }

    @Override
    public List<String> readMessage(String file, long position, int count, boolean includeFileHeader) {
        return storeManageService.readMessage(file, position, count, includeFileHeader);
    }

    @Override
    public IndexItem [] readPartitionIndices(String topic, short partition, long index, int count) {
        return storeManageService.readPartitionIndices(topic, partition, index, count);
    }

    @Override
    public IndexItem [] readIndices(String file, long position, int count, boolean includeFileHeader) {
        return storeManageService.readIndices(file, position, count, includeFileHeader);
    }

    @Override
    public String readFile(String file, long position, int length) {
        return storeManageService.readFile(file, position, length);
    }

    @Override
    public String readPartitionGroupStore(String topic, int partitionGroup, long position, int length) {
        return storeManageService.readPartitionGroupStore(topic, partitionGroup, position, length);
    }

    @Override
    public boolean initCoordinator() {
        return coordinatorManageService.initCoordinator();
    }

    @Override
    public boolean removeCoordinatorGroup(String namespace, String groupId) {
        return coordinatorManageService.removeCoordinatorGroup(namespace, groupId);
    }

    @Override
    public void restoreElectionMetadata() {
        electionManageService.restoreElectionMetadata();
    }


    @Override
    public String describe() {
        logger.info("Describe");
        return electionManageService.describe();
    }

    @Override
    public String describeTopic(String topic, int partitionGroup) {
        return electionManageService.describeTopic(topic, partitionGroup);
    }

    @Override
    public void updateTerm(String topic, int partitionGroup, int term) {
        electionManageService.updateTerm(topic, partitionGroup, term);
    }

}