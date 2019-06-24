/**
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
package com.jd.joyqueue.service;

import com.jd.joyqueue.model.domain.Broker;
import com.jd.joyqueue.model.query.QBroker;
import com.jd.joyqueue.nsr.NsrService;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public interface BrokerService extends NsrService<Broker,QBroker,Long> {

//    /**
//     * 生成Broker
//     * @param model
//     * @return
//     */
//    void generateBroker(Hosts hosts, Broker model);
//
//    Broker findByIp(String ip);

    List<Broker> getByIdsBroker(List<Integer> ids) throws Exception;
    /**
     * 同步所有broker
     * @throws Exception
     */
    List<Broker> syncBrokers() throws Exception;

    List<Broker> findByTopic(String topic) throws Exception;

    List<Broker> queryBrokerList(QBroker qBroker) throws Exception;

}
