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
package io.chubao.joyqueue.nsr.service;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.nsr.model.BrokerQuery;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface BrokerService extends DataService<Broker, BrokerQuery, Integer> {

    /**
     * 根据IP和端口获取Broker
     *
     * @param brokerIp
     * @param brokerPort
     * @return
     */
    Broker getByIpAndPort(String brokerIp, Integer brokerPort);

    /**
     * 根据重试类型查询broker
     * @param retryType
     * @return
     */
    List<Broker> getByRetryType(String retryType);

    /**
     * 根据ids 查询所有broker集合
     * @param ids
     * @return
     */
    List<Broker> getByIds(List<Integer> ids);


    void update(Broker broker);
}
