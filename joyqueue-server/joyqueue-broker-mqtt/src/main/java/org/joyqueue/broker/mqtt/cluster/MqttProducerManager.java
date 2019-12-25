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
package org.joyqueue.broker.mqtt.cluster;

import org.joyqueue.broker.mqtt.connection.MqttConnection;
import org.joyqueue.network.session.Producer;
import com.google.common.base.Strings;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author majun8
 */
public class MqttProducerManager extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(MqttProducerManager.class);

    private ConcurrentMap<String, Producer> producers = new ConcurrentHashMap<>();
    private MqttConnectionManager connectionManager;

    public MqttProducerManager(MqttConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void removeProducer(String clientID) {
        if (connectionManager.isConnected(clientID)) {
            MqttConnection connection = connectionManager.getConnection(clientID);
            ConcurrentMap<String, ConcurrentMap<String, String>> topicProducers = connection.getProducers();
            if (topicProducers != null) {
                for (String topic : topicProducers.keySet()) {
                    ConcurrentMap<String, String> applicationProducers = topicProducers.get(topic);
                    if (applicationProducers != null) {
                        for (String application : applicationProducers.keySet()) {
                            String producerId = applicationProducers.get(application);
                            if (!Strings.isNullOrEmpty(producerId)) {
                                producers.remove(producerId);
                            }
                        }
                    }
                }
            }
        }
    }

    public Producer getProducer(String clientID, String application, String topic) {
        Producer producer = null;
        if (connectionManager.isConnected(clientID) && (producer = producers.get(generateProducerId(clientID, topic, application))) == null) {
            MqttConnection connection = connectionManager.getConnection(clientID);
            String producerId = connection.getProducer(application, topic);
            if (Strings.isNullOrEmpty(producerId)) {
                producerId = generateProducerId(clientID, topic, application);
                producer = new Producer();
                producer.setId(producerId);
                producer.setConnectionId(connection.getId());
                producer.setApp(application);
                producer.setTopic(topic);
                producer.setType(Producer.ProducerType.MQTT);
                Producer oldProducer = producers.putIfAbsent(producerId, producer);
                if (oldProducer != null) {
                    producer = oldProducer;
                    connection.addProducer(topic, application, producerId);
                }
            } else {
                return producers.get(producerId);
            }
        }
        return producer;
    }

    private String generateProducerId(String clientID, String topic, String application) {
        return String.format("%s_producer_%s_%s", clientID, application, topic);
    }
}
