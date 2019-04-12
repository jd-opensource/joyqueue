package com.jd.journalq.broker.kafka.handler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.command.OffsetFetchRequest;
import com.jd.journalq.broker.kafka.command.OffsetFetchResponse;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OffsetFetchRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class OffsetFetchRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(OffsetFetchRequestHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        OffsetFetchRequest offsetFetchRequest = (OffsetFetchRequest) command.getPayload();
        String groupId = offsetFetchRequest.getGroupId();
        HashMultimap<String, Integer> topicAndPartitions = offsetFetchRequest.getTopicAndPartitions();
        Table<String, Integer, OffsetMetadataAndError> topicPartitionOffsetMetadataMap = groupCoordinator.handleFetchOffsets(groupId, topicAndPartitions);

        // TODO 临时日志
        logger.info("fetch offset, request: {}, response: {}", offsetFetchRequest, topicPartitionOffsetMetadataMap);
        OffsetFetchResponse offsetFetchResponse = new OffsetFetchResponse(topicPartitionOffsetMetadataMap);
        return new Command(offsetFetchResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }
}
