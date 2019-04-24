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
package com.jd.journalq.convert;


import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.Identity;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public class NsrBrokerConverter extends Converter<Broker, com.jd.journalq.domain.Broker> {
    @Override
    protected com.jd.journalq.domain.Broker forward(Broker broker) {
        com.jd.journalq.domain.Broker nsrBroker = new com.jd.journalq.domain.Broker();
        nsrBroker.setId(Long.valueOf(String.valueOf(broker.getId())).intValue());
        if (broker.getIp() != null) {
            nsrBroker.setIp(broker.getIp());
        }
        nsrBroker.setPort(broker.getPort());
        if (broker.getRetryType() != null) {
            nsrBroker.setRetryType(broker.getRetryType());
        }
        if(broker.getDataCenter() != null) {
            nsrBroker.setDataCenter(broker.getDataCenter().getCode());
        }
        if (broker.getPermission() != null) {
            nsrBroker.setPermission(broker.getPermission());
        }
        return nsrBroker;
    }

    @Override
    protected Broker backward(com.jd.journalq.domain.Broker nsrBroker) {
        Broker broker = new Broker();
        broker.setId(nsrBroker.getId());
        broker.setIp(nsrBroker.getIp());
        broker.setPort(nsrBroker.getPort());
        broker.setPermission(nsrBroker.getPermission());

        broker.setRetryType(nsrBroker.getRetryType());
        if (broker.getDataCenter() != null) {
            broker.setDataCenter(new Identity(nsrBroker.getDataCenter()));
        }
        return broker;
    }
}
