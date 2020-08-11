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
package org.joyqueue.nsr.support;

import org.joyqueue.domain.Broker;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.event.UpdateBrokerEvent;
import org.joyqueue.nsr.exception.NsrException;
import org.joyqueue.nsr.message.Messenger;
import org.joyqueue.nsr.model.BrokerQuery;
import org.joyqueue.nsr.service.BrokerService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
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
    private NameServiceConfig config;
    private Messenger messenger;

    public DefaultBrokerService(BrokerInternalService brokerInternalService, TransactionInternalService transactionInternalService,
                                NameServiceConfig config, Messenger messenger) {
        this.brokerInternalService = brokerInternalService;
        this.transactionInternalService = transactionInternalService;
        this.config = config;
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
            throw new NsrException(String.format("broker: %s does not exist", broker.getId()));
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
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("updateBroker exception, broker: {}", broker, e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishBrokerEnable()) {
            messenger.publish(new UpdateBrokerEvent(oldBroker, broker), broker);
        }
        return broker;
    }

    @Override
    public void delete(int id) {
        brokerInternalService.delete(id);
    }
}
