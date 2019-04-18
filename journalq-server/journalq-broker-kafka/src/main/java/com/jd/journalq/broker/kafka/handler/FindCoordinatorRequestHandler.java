package com.jd.journalq.broker.kafka.handler;


import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.FindCoordinatorRequest;
import com.jd.journalq.broker.kafka.command.FindCoordinatorResponse;
import com.jd.journalq.broker.kafka.coordinator.CoordinatorType;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.broker.kafka.model.KafkaBroker;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.Subscription;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.nsr.NameService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FindCoordinatorRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class FindCoordinatorRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FindCoordinatorRequestHandler.class);

    private GroupCoordinator groupCoordinator;
    private TransactionCoordinator transactionCoordinator;
    private NameService nameService;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
        this.transactionCoordinator = kafkaContext.getTransactionCoordinator();
        this.nameService = kafkaContext.getBrokerContext().getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FindCoordinatorRequest findCoordinatorRequest = (FindCoordinatorRequest) command.getPayload();
        CoordinatorType coordinatorType = ObjectUtils.defaultIfNull(findCoordinatorRequest.getCoordinatorType(), CoordinatorType.GROUP);
        String coordinatorKey = findCoordinatorRequest.getCoordinatorKey();
        if (coordinatorType.equals(CoordinatorType.TRANSACTION)) {
            coordinatorKey = KafkaClientHelper.parseClient(findCoordinatorRequest.getClientId());
        }

        if (StringUtils.isBlank(coordinatorKey)) {
            logger.warn("coordinatorKey error, coordinatorKey: {}", coordinatorKey);
            return new Command(new FindCoordinatorResponse(KafkaErrorCode.INVALID_GROUP_ID.getCode(), KafkaBroker.INVALID));
        }

        coordinatorKey = KafkaClientHelper.parseClient(coordinatorKey);
        Broker coordinator = null;
        if (coordinatorType.equals(CoordinatorType.GROUP)) {
            if (!nameService.hasSubscribe(coordinatorKey, Subscription.Type.CONSUMPTION)) {
                logger.warn("find subscribe for coordinatorKey {}, subscribe not exist", coordinatorKey);
                return new Command(new FindCoordinatorResponse(KafkaErrorCode.INVALID_GROUP_ID.getCode(), KafkaBroker.INVALID));
            }
            coordinator = groupCoordinator.findCoordinator(coordinatorKey);
        } else if (coordinatorType.equals(CoordinatorType.TRANSACTION)) {
            if (!nameService.hasSubscribe(coordinatorKey, Subscription.Type.PRODUCTION)) {
                logger.warn("find subscribe for coordinatorKey {}, subscribe not exist", coordinatorKey);
                return new Command(new FindCoordinatorResponse(KafkaErrorCode.INVALID_GROUP_ID.getCode(), KafkaBroker.INVALID));
            }
            coordinator = transactionCoordinator.findCoordinator(coordinatorKey);
        }

        if (coordinator == null) {
            logger.error("find coordinator for coordinatorKey {}, coordinator is null", coordinatorKey);
            return new Command(new FindCoordinatorResponse(KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode(), KafkaBroker.INVALID));
        }

        logger.info("find coordinator for coordinatorKey {}, broker: {id: {}, ip: {}, port: {}}", coordinatorKey, coordinator.getId(), coordinator.getIp(), coordinator.getPort());
        FindCoordinatorResponse response = new FindCoordinatorResponse(KafkaErrorCode.NONE.getCode(),
                new KafkaBroker(coordinator.getId(), coordinator.getIp(), coordinator.getPort()));
        return new Command(response);
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }
}