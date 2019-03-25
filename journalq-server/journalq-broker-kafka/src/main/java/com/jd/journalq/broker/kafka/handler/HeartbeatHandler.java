package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.coordinator.GroupCoordinator;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.command.HeartbeatRequest;
import com.jd.journalq.broker.kafka.command.HeartbeatResponse;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HeartbeatHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class HeartbeatHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        HeartbeatRequest heartbeatRequest = (HeartbeatRequest) command.getPayload();
        short errorCode = groupCoordinator.handleHeartbeat(heartbeatRequest.getGroupId(), heartbeatRequest.getMemberId(), heartbeatRequest.getGroupGenerationId());
        HeartbeatResponse heartbeatResponse = new HeartbeatResponse(errorCode);
        return new Command(heartbeatResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.HEARTBEAT.getCode();
    }
}
