package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.BrokerConverter;
import io.chubao.joyqueue.nsr.journalkeeper.domain.BrokerDTO;
import io.chubao.joyqueue.nsr.journalkeeper.repository.BrokerRepository;
import io.chubao.joyqueue.nsr.model.BrokerQuery;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;

import java.util.List;

/**
 * JournalkeeperBrokerInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperBrokerInternalService implements BrokerInternalService {

    private BrokerRepository brokerRepository;

    public JournalkeeperBrokerInternalService(BrokerRepository brokerRepository) {
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
    public Broker update(Broker broker) {
        return BrokerConverter.convert(brokerRepository.update(BrokerConverter.convert(broker)));
    }

    @Override
    public Broker getById(int id) {
        return BrokerConverter.convert(brokerRepository.getById(id));
    }

    @Override
    public Broker add(Broker broker) {
        return BrokerConverter.convert(brokerRepository.add(BrokerConverter.convert(broker)));
    }

    @Override
    public void delete(int id) {
        brokerRepository.deleteById(id);
    }

    @Override
    public List<Broker> getAll() {
        return BrokerConverter.convert(brokerRepository.getAll());
    }

    @Override
    public PageResult<Broker> search(QPageQuery<BrokerQuery> pageQuery) {
        int count = brokerRepository.getSearchCount(pageQuery.getQuery());
        List<BrokerDTO> brokers = null;
        if (count != 0) {
            brokers = brokerRepository.search(pageQuery);
        }

        Pagination pagination = pageQuery.getPagination();
        pagination.setTotalRecord(count);

        PageResult<Broker> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(BrokerConverter.convert(brokers));
        return result;
    }
}