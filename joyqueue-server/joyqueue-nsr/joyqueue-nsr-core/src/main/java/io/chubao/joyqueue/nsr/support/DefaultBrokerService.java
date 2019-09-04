package io.chubao.joyqueue.nsr.support;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.event.UpdateBrokerEvent;
import io.chubao.joyqueue.nsr.exception.NsrException;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.model.BrokerQuery;
import io.chubao.joyqueue.nsr.service.BrokerService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * BrokerService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultBrokerService implements BrokerService {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultBrokerService.class);

    private BrokerInternalService brokerInternalService;
    private TransactionInternalService transactionInternalService;
    private Messenger messenger;

    public DefaultBrokerService(BrokerInternalService brokerInternalService, TransactionInternalService transactionInternalService, Messenger messenger) {
        this.brokerInternalService = brokerInternalService;
        this.transactionInternalService = transactionInternalService;
        this.messenger = messenger;
    }

    @Override
    public Broker getById(int id) {
        return brokerInternalService.getById(id);
    }

    @Override
    public Broker getByIpAndPort(String brokerIp, Integer brokerPort) {
        return brokerInternalService.getByIpAndPort(brokerIp, brokerPort);
    }

    @Override
    public List<Broker> getByRetryType(String retryType) {
        return brokerInternalService.getByRetryType(retryType);
    }

    @Override
    public List<Broker> getByIds(List<Integer> ids) {
        return brokerInternalService.getByIds(ids);
    }

    @Override
    public List<Broker> getAll() {
        return brokerInternalService.getAll();
    }

    @Override
    public PageResult<Broker> search(QPageQuery<BrokerQuery> pageQuery) {
        return brokerInternalService.search(pageQuery);
    }

    @Override
    public Broker add(Broker broker) {
        return brokerInternalService.add(broker);
    }

    @Override
    public Broker update(Broker broker) {
        Broker oldBroker = brokerInternalService.getById(broker.getId());
        if (oldBroker == null) {
            throw new NsrException(String.format("broker: %s is not exist", broker.getId()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, id: {}", broker.getId(), e);
            throw new NsrException(e);
        }

        logger.info("updateBroker, id: {}", broker.getId());

        try {
            brokerInternalService.update(broker);
            messenger.publish(new UpdateBrokerEvent(oldBroker, broker), broker);
            transactionInternalService.commit();
            return broker;
        } catch (Exception e) {
            logger.error("updateBroker exception, id: {}", broker, e);
            messenger.fastPublish(new UpdateBrokerEvent(broker, oldBroker), broker);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void delete(int id) {
        brokerInternalService.delete(id);
    }
}