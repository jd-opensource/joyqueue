package com.jd.journalq.broker.kafka.handler;


import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.coordinator.GroupCoordinator;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.FindCoordinatorRequest;
import com.jd.journalq.broker.kafka.command.FindCoordinatorResponse;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.broker.kafka.model.KafkaBroker;
import com.jd.journalq.common.domain.Broker;
import com.jd.journalq.common.domain.Subscription;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FindCoordinatorHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class FindCoordinatorHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FindCoordinatorHandler.class);

    private GroupCoordinator groupCoordinator;
    private ClusterManager clusterManager;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
        this.clusterManager = kafkaContext.getBrokerContext().getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FindCoordinatorRequest findCoordinatorRequest = (FindCoordinatorRequest) command.getPayload();
        String groupId = KafkaClientHelper.parseClient(findCoordinatorRequest.getGroupId());

        if (StringUtils.isBlank(groupId)) {
            logger.warn("groupId error, groupId: {}", groupId);
            return new Command(new FindCoordinatorResponse(KafkaErrorCode.INVALID_GROUP_ID, KafkaBroker.INVALID));
        }

        if (!clusterManager.hasSubscribe(groupId, Subscription.Type.CONSUMPTION)) {
            logger.warn("find subscribe for group {}, subscribe not exist", groupId);
            return new Command(new FindCoordinatorResponse(KafkaErrorCode.INVALID_GROUP_ID, KafkaBroker.INVALID));
        }

        Broker coordinator = groupCoordinator.findCoordinator(groupId);
        if (coordinator == null) {
            logger.error("find coordinator for group {}, coordinator is null", groupId);
            return new Command(new FindCoordinatorResponse(KafkaErrorCode.NOT_COORDINATOR_FOR_CONSUMER, KafkaBroker.INVALID));
        }

        logger.info("find coordinator for group, broker: {id: {}, ip: {}, port: {}}", coordinator.getId(), coordinator.getIp(), coordinator.getPort());
        FindCoordinatorResponse findCoordinatorResponse = new FindCoordinatorResponse(KafkaErrorCode.NONE, new KafkaBroker(coordinator.getId(), coordinator.getIp(), coordinator.getPort()));
        return new Command(findCoordinatorResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }
}