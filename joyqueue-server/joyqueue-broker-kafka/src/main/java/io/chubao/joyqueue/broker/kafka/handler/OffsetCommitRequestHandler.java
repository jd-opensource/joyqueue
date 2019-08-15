package io.chubao.joyqueue.broker.kafka.handler;


import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.command.OffsetCommitRequest;
import io.chubao.joyqueue.broker.kafka.command.OffsetCommitResponse;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import io.chubao.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * OffsetCommitRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class OffsetCommitRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(OffsetCommitRequestHandler.class);

    private GroupCoordinator groupCoordinator;
    private KafkaConfig config;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
        this.config = kafkaContext.getConfig();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        OffsetCommitRequest offsetCommitRequest = (OffsetCommitRequest) command.getPayload();

        Map<String, List<OffsetMetadataAndError>> result = groupCoordinator.handleCommitOffsets(offsetCommitRequest.getGroupId(), offsetCommitRequest.getMemberId(),
                offsetCommitRequest.getGroupGenerationId(), offsetCommitRequest.getOffsets());

        if (config.getLogDetail(offsetCommitRequest.getClientId())) {
            logger.info("offset commit request with correlation id {} from transport: {}, client {}, request: {}, result: {}",
                    transport, offsetCommitRequest.getCorrelationId(), offsetCommitRequest.getGroupId(), offsetCommitRequest, result);
        }

        OffsetCommitResponse offsetCommitResponse = new OffsetCommitResponse(result);
        return new Command(offsetCommitResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}