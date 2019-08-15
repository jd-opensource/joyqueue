package io.chubao.joyqueue.broker.manage.service.support;

import io.chubao.joyqueue.broker.manage.service.BrokerManageService;
import io.chubao.joyqueue.broker.manage.service.ConnectionManageService;
import io.chubao.joyqueue.broker.manage.service.ConsumerManageService;
import io.chubao.joyqueue.broker.manage.service.CoordinatorManageService;
import io.chubao.joyqueue.broker.manage.service.ElectionManageService;
import io.chubao.joyqueue.broker.manage.service.MessageManageService;
import io.chubao.joyqueue.broker.manage.service.StoreManageService;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.manage.IndexItem;
import io.chubao.joyqueue.manage.PartitionGroupMetric;
import io.chubao.joyqueue.manage.PartitionMetric;
import io.chubao.joyqueue.manage.TopicMetric;
import io.chubao.joyqueue.monitor.BrokerMessageInfo;
import io.chubao.joyqueue.monitor.PartitionAckMonitorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 *
 * author: gaohaoxiang
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
    public boolean setAckIndex(String topic, String app, short partition, long index) throws JoyQueueException {
        return consumerManageService.setAckIndex(topic, app, partition, index);
    }

    @Override
    public boolean setMaxAckIndex(String topic, String app, short partition) throws JoyQueueException {
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
    public boolean setMaxAckIndexes(String topic, String app) throws JoyQueueException {
        return consumerManageService.setMaxAckIndexes(topic, app);
    }

    @Override
    public boolean setAckIndexByTime(String topic, String app, short partition, long timestamp) throws JoyQueueException {
        return consumerManageService.setAckIndexByTime(topic, app, partition, timestamp);
    }

    @Override
    public boolean setAckIndexesByTime(String topic, String app, long timestamp) throws JoyQueueException {
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