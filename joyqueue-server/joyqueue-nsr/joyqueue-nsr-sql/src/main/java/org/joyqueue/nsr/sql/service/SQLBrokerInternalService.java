/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.sql.service;

import org.joyqueue.domain.Broker;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.sql.converter.BrokerConverter;
import org.joyqueue.nsr.sql.domain.BrokerDTO;
import org.joyqueue.nsr.sql.repository.BrokerRepository;
import org.joyqueue.nsr.model.BrokerQuery;
import org.joyqueue.nsr.service.internal.BrokerInternalService;

import java.util.List;

/**
 * SQLBrokerInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLBrokerInternalService implements BrokerInternalService {

    private BrokerRepository brokerRepository;

    public SQLBrokerInternalService(BrokerRepository brokerRepository) {
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