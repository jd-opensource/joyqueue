package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.BrokerConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.BrokerRepository;
import io.chubao.joyqueue.nsr.model.BrokerQuery;
import io.chubao.joyqueue.nsr.service.BrokerService;

import java.util.List;

/**
 * JournalkeeperBrokerService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperBrokerService implements BrokerService {

    private BrokerRepository brokerRepository;

    public JournalkeeperBrokerService(BrokerRepository brokerRepository) {
        this.brokerRepository = brokerRepository;
    }

    @Override
    public Broker getByIpAndPort(String brokerIp, Integer brokerPort) {
        return BrokerConverter.convert(brokerRepository.getByIpAndPort(brokerIp, brokerPort));
    }

    @Override
    public List<Broker> getByRetryType(String retryType) {
        return BrokerConverter.convert(brokerRepository.getByRetryType(retryType));
    }

    @Override
    public List<Broker> getByIds(List<Integer> ids) {
        return BrokerConverter.convert(brokerRepository.getByIds(ids));
    }

    @Override
    public void update(Broker broker) {

    }

    @Override
    public Broker getById(Integer id) {
        return BrokerConverter.convert(brokerRepository.getById(id));
    }

    @Override
    public Broker get(Broker model) {
        return getById(model.getId());
    }

    @Override
    public void addOrUpdate(Broker broker) {
        if (getById(broker.getId()) != null) {
            return;
        }
        brokerRepository.add(BrokerConverter.convert(broker));
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