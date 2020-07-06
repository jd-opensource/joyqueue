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
package org.joyqueue.nsr.ignite.service;


import com.alibaba.fastjson.JSON;
import com.google.inject.Inject;
import org.joyqueue.domain.Broker;
import org.joyqueue.event.BrokerEvent;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.ignite.dao.BrokerDao;
import org.joyqueue.nsr.ignite.message.IgniteMessenger;
import org.joyqueue.nsr.ignite.model.IgniteBroker;
import org.joyqueue.nsr.model.BrokerQuery;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public class IgniteBrokerInternalService implements BrokerInternalService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private BrokerDao brokerDao;

    @Inject
    protected IgniteMessenger messenger;

    @Inject
    public IgniteBrokerInternalService(BrokerDao brokerDao) {
        this.brokerDao = brokerDao;
    }


    public IgniteBroker toIgniteModel(Broker model) {
        return new IgniteBroker(model);
    }

    @Override
    public Broker getByIpAndPort(String brokerIp, Integer brokerPort) {
        BrokerQuery brokerQuery = new BrokerQuery();
        brokerQuery.setIp(brokerIp);
        brokerQuery.setPort(brokerPort);
        List<IgniteBroker> list = brokerDao.list(brokerQuery);
        if (null == list || list.size() < 1) {
            return null;
        }

        if (list.size() > 1) {
            throw new RuntimeException("illegal state exception.too many brokers.");
        }
        return list.get(0);
    }

    @Override
    public List<Broker> getByRetryType(String retryType) {
        BrokerQuery query = new BrokerQuery();
        query.setRetryType(retryType);

        return convert(brokerDao.list(query));
    }

    @Override
    public Broker getById(int id) {
        return brokerDao.findById(id);
    }

    @Override
    public List<Broker> getByIds(List<Integer> ids) {
        if (ids == null || ids.size() <=0){
            return Collections.emptyList();
        }
        return ids.stream().map(brokerId-> brokerDao.findById(brokerId)).filter(broker -> broker != null).collect(Collectors.toList());
    }

    @Override
    public List<Broker> getAll() {
        return convert(brokerDao.list(new BrokerQuery()));
    }

    @Override
    public Broker add(Broker broker) {
        brokerDao.add(toIgniteModel(broker));
        publishEvent(BrokerEvent.event(broker));
        return broker;
    }

    public void publishEvent(MetaEvent event) {
        try {
            logger.info("publishEvent {}", event);
            messenger.publish(event);
        } catch (Exception ignored) {
            logger.warn("pulish event failure {}", event);
        }
    }

    @Override
    public Broker update(Broker broker) {
        try {
            brokerDao.addOrUpdate(new IgniteBroker(broker));
            this.publishEvent(BrokerEvent.event(broker));
            return broker;
        } catch (Exception e) {
            String message = String.format("update broker [%s] error", JSON.toJSON(broker));
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public void delete(int id) {
        brokerDao.deleteById(id);
    }

    @Override
    public PageResult<Broker> search(QPageQuery<BrokerQuery> pageQuery) {
        PageResult<IgniteBroker> iBrokers = brokerDao.pageQuery(pageQuery);

        return new PageResult<>(iBrokers.getPagination(), convert(iBrokers.getResult()));
    }

    private List<Broker> convert(List<IgniteBroker> iBrokers) {
        if (iBrokers == null) {
            return Collections.EMPTY_LIST;
        }

        return new ArrayList<>(iBrokers);
    }
}
