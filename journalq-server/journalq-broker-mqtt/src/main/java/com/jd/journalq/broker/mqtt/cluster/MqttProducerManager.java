package com.jd.journalq.broker.mqtt.cluster;

import com.jd.journalq.broker.mqtt.connection.MqttConnection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.toolkit.lang.Strings;
import com.jd.journalq.toolkit.service.Service;
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
