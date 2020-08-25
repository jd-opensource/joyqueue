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
package org.joyqueue.nsr.composition.service;

import org.joyqueue.domain.Broker;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.model.BrokerQuery;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionBrokerInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionBrokerInternalService implements BrokerInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionBrokerInternalService.class);

    private CompositionConfig config;
    private BrokerInternalService sourceBrokerService;
    private BrokerInternalService targetBrokerService;

    public CompositionBrokerInternalService(CompositionConfig config, BrokerInternalService sourceBrokerService,
                                            BrokerInternalService targetBrokerService) {
        this.config = config;
        this.sourceBrokerService = sourceBrokerService;
        this.targetBrokerService = targetBrokerService;
    }

    @Override
    public Broker getByIpAndPort(String brokerIp, Integer brokerPort) {
        if (config.isReadSource()) {
            return sourceBrokerService.getByIpAndPort(brokerIp, brokerPort);
        } else {
            try {
                return targetBrokerService.getByIpAndPort(brokerIp, brokerPort);
            } catch (Exception e) {
                logger.error("getByIpAndPort exception, brokerIp: {}, brokerPort: {}", brokerIp, brokerPort, e);
                return sourceBrokerService.getByIpAndPort(brokerIp, brokerPort);
            }
        }
    }

    @Override
    public List<Broker> getByRetryType(String retryType) {
        if (config.isReadSource()) {
            return sourceBrokerService.getByRetryType(retryType);
        } else {
            try {
                return targetBrokerService.getByRetryType(retryType);
            } catch (Exception e) {
                logger.error("getByRetryType exception, retryType: {}", retryType, e);
                return sourceBrokerService.getByRetryType(retryType);
            }
        }
    }

    @Override
    public List<Broker> getByIds(List<Integer> ids) {
        if (config.isReadSource()) {
            return sourceBrokerService.getByIds(ids);
        } else {
            try {
                return targetBrokerService.getByIds(ids);
            } catch (Exception e) {
                logger.error("getByIds exception, ids: {}", ids, e);
                return sourceBrokerService.getByIds(ids);
            }
        }
    }

    @Override
    public Broker update(Broker broker) {
        Broker result = null;
        if (config.isWriteSource()) {
            result = sourceBrokerService.update(broker);
        }
        if (config.isWriteTarget()) {
            try {
                targetBrokerService.update(broker);
            } catch (Exception e) {
                logger.error("update exception, params: {}", broker, e);
            }
        }
        return result;
    }

    @Override
    public Broker getById(int id) {
        if (config.isReadSource()) {
            return sourceBrokerService.getById(id);
        } else {
            try {
                return targetBrokerService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id", id, e);
                return sourceBrokerService.getById(id);
            }
        }
    }

    @Override
    public Broker add(Broker broker) {
        Broker result = null;
        if (config.isWriteSource()) {
            result = sourceBrokerService.add(broker);
        }
        if (config.isWriteTarget()) {
            try {
                targetBrokerService.add(broker);
            } catch (Exception e) {
                logger.error("addOrUpdate exception, params: {}", broker, e);
            }
        }
        return result;
    }

    @Override
    public void delete(int id) {
        if (config.isWriteSource()) {
            sourceBrokerService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetBrokerService.delete(id);
            } catch (Exception e) {
                logger.error("deleteById exception, params: {}", id, e);
            }
        }
    }

    @Override
    public List<Broker> getAll() {
        if (config.isReadSource()) {
            return sourceBrokerService.getAll();
        } else {
            try {
                return targetBrokerService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourceBrokerService.getAll();
            }
        }
    }

    @Override
    public PageResult<Broker> search(QPageQuery<BrokerQuery> pageQuery) {
        if (config.isReadSource()) {
            return sourceBrokerService.search(pageQuery);
        } else {
            try {
                return targetBrokerService.search(pageQuery);
            } catch (Exception e) {
                logger.error("search exception, pageQuery: {}", pageQuery, e);
                return sourceBrokerService.search(pageQuery);
            }
        }
    }
}
