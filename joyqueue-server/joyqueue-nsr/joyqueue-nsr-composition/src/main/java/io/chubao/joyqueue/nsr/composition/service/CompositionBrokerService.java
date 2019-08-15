package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.BrokerQuery;
import io.chubao.joyqueue.nsr.service.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionBrokerService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionBrokerService implements BrokerService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionBrokerService.class);

    private CompositionConfig config;
    private BrokerService igniteBrokerService;
    private BrokerService journalkeeperBrokerService;

    public CompositionBrokerService(CompositionConfig config, BrokerService igniteBrokerService,
                                    BrokerService journalkeeperBrokerService) {
        this.config = config;
        this.igniteBrokerService = igniteBrokerService;
        this.journalkeeperBrokerService = journalkeeperBrokerService;
    }

    @Override
    public Broker getByIpAndPort(String brokerIp, Integer brokerPort) {
        if (config.isReadIgnite()) {
            return igniteBrokerService.getByIpAndPort(brokerIp, brokerPort);
        } else {
            return journalkeeperBrokerService.getByIpAndPort(brokerIp, brokerPort);
        }
    }

    @Override
    public List<Broker> getByRetryType(String retryType) {
        if (config.isReadIgnite()) {
            return igniteBrokerService.getByRetryType(retryType);
        } else {
            return journalkeeperBrokerService.getByRetryType(retryType);
        }
    }

    @Override
    public List<Broker> getByIds(List<Integer> ids) {
        if (config.isReadIgnite()) {
            return igniteBrokerService.getByIds(ids);
        } else {
            return journalkeeperBrokerService.getByIds(ids);
        }
    }

    @Override
    public void update(Broker broker) {
        if (config.isWriteIgnite()) {
            igniteBrokerService.update(broker);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperBrokerService.update(broker);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", broker, e);
            }
        }
    }

    @Override
    public Broker getById(Integer id) {
        if (config.isReadIgnite()) {
            return igniteBrokerService.getById(id);
        } else {
            return journalkeeperBrokerService.getById(id);
        }
    }

    @Override
    public Broker get(Broker model) {
        if (config.isReadIgnite()) {
            return igniteBrokerService.get(model);
        } else {
            return journalkeeperBrokerService.get(model);
        }
    }

    @Override
    public void addOrUpdate(Broker broker) {
        if (config.isWriteIgnite()) {
            igniteBrokerService.addOrUpdate(broker);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperBrokerService.addOrUpdate(broker);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", broker, e);
            }
        }
    }

    @Override
    public void deleteById(Integer id) {
        if (config.isWriteIgnite()) {
            igniteBrokerService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperBrokerService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(Broker model) {
        if (config.isWriteIgnite()) {
            igniteBrokerService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperBrokerService.delete(model);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<Broker> list() {
        if (config.isReadIgnite()) {
            return igniteBrokerService.list();
        } else {
            return journalkeeperBrokerService.list();
        }
    }

    @Override
    public List<Broker> list(BrokerQuery query) {
        if (config.isReadIgnite()) {
            return igniteBrokerService.list(query);
        } else {
            return journalkeeperBrokerService.list(query);
        }
    }

    @Override
    public PageResult<Broker> pageQuery(QPageQuery<BrokerQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteBrokerService.pageQuery(pageQuery);
        } else {
            return journalkeeperBrokerService.pageQuery(pageQuery);
        }
    }
}
