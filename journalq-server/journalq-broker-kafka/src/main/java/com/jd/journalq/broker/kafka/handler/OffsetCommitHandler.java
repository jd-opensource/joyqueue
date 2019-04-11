package com.jd.journalq.broker.kafka.handler;


import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.OffsetCommitRequest;
import com.jd.journalq.broker.kafka.command.OffsetCommitResponse;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * OffsetCommitHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class OffsetCommitHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(OffsetCommitHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        OffsetCommitRequest offsetCommitRequest = (OffsetCommitRequest) command.getPayload();

        Table<String, Integer, OffsetMetadataAndError> result = groupCoordinator.handleCommitOffsets(offsetCommitRequest.getGroupId(), offsetCommitRequest.getMemberId(),
                offsetCommitRequest.getGroupGenerationId(), offsetCommitRequest.getOffsetAndMetadata());

        if (logger.isDebugEnabled()) {
            for (String topic : result.rowKeySet()) {
                Map<Integer, OffsetMetadataAndError> errorCodes = result.row(topic);
                for (Map.Entry<Integer, OffsetMetadataAndError> codeEntry : errorCodes.entrySet()) {
                    if (codeEntry.getValue().getError() == KafkaErrorCode.NONE.getCode()) {
                        logger.debug("offset commit request with correlation id {} from client {} on partition {} offset {}",
                                offsetCommitRequest.getGroupId(), offsetCommitRequest.getClientId(), topic, codeEntry.getValue());
                    } else {
                        logger.debug("offset commit request with correlation id {} from client {} on partition {} failed due to {}",
                                offsetCommitRequest.getGroupId(), offsetCommitRequest.getClientId(), topic, codeEntry.getValue());
                    }
                }
            }
        }

        OffsetCommitResponse offsetCommitResponse = new OffsetCommitResponse(result);
        return new Command(offsetCommitResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}