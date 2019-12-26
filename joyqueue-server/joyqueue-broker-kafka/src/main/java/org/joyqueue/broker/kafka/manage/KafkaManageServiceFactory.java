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
package org.joyqueue.broker.kafka.manage;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.manage.support.DefaultKafkaGroupManageService;
import org.joyqueue.broker.kafka.manage.support.DefaultKafkaManageService;
import org.joyqueue.broker.kafka.manage.support.DefaultKafkaMonitorService;

/**
 * KafkaManageServiceFactory
 *
 * author: gaohaoxiang
 * date: 2018/11/13
 */
public class KafkaManageServiceFactory {

    private KafkaManageService kafkaManageService;
    private KafkaMonitorService kafkaMonitorService;

    public KafkaManageServiceFactory(BrokerContext brokerContext, KafkaContext kafkaContext) {
        this.kafkaManageService = newKafkaManageService(brokerContext, kafkaContext);
        this.kafkaMonitorService = newKafkaMonitorService(brokerContext, kafkaContext);
    }

    public KafkaManageService getKafkaManageService() {
        return kafkaManageService;
    }

    public KafkaMonitorService getKafkaMonitorService() {
        return kafkaMonitorService;
    }

    protected KafkaManageService newKafkaManageService(BrokerContext brokerContext, KafkaContext kafkaContext) {
        DefaultKafkaGroupManageService kafkaGroupManageService = new DefaultKafkaGroupManageService(kafkaContext.getGroupCoordinator());
        return new DefaultKafkaManageService(kafkaGroupManageService);
    }

    protected KafkaMonitorService newKafkaMonitorService(BrokerContext brokerContext, KafkaContext kafkaContext) {
        return new DefaultKafkaMonitorService();
    }
}