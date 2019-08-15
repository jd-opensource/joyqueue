package io.chubao.joyqueue.broker.kafka.handler;

import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.command.HeartbeatRequest;
import io.chubao.joyqueue.broker.kafka.command.HeartbeatResponse;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HeartbeatRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class HeartbeatRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(HeartbeatRequestHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        HeartbeatRequest heartbeatRequestRequest = (HeartbeatRequest) command.getPayload();
        short errorCode = groupCoordinator.handleHeartbeat(heartbeatRequestRequest.getGroupId(), heartbeatRequestRequest.getMemberId(), heartbeatRequestRequest.getGroupGenerationId());
        HeartbeatResponse heartbeatResponse = new HeartbeatResponse(errorCode);
        return new Command(heartbeatResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.HEARTBEAT.getCode();
    }
}
