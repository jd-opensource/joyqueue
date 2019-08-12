package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.domain.Broker;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.model.BrokerQuery;
import com.jd.joyqueue.nsr.service.BrokerService;

import java.util.List;

/**
 * CompositionBrokerService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionBrokerService implements BrokerService {

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
        return null;
    }

    @Override
    public List<Broker> getByRetryType(String retryType) {
        return null;
    }

    @Override
    public List<Broker> getByIds(List<Integer> ids) {
        return null;
    }

    @Override
    public void update(Broker broker) {

    }

    @Override
    public Broker getById(Integer id) {
        return null;
    }

    @Override
    public Broker get(Broker model) {
        return null;
    }

    @Override
    public void addOrUpdate(Broker broker) {

    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public void delete(Broker model) {

    }

    @Override
    public List<Broker> list() {
        return null;
    }

    @Override
    public List<Broker> list(BrokerQuery query) {
        return null;
    }

    @Override
    public PageResult<Broker> pageQuery(QPageQuery<BrokerQuery> pageQuery) {
        return null;
    }
}
