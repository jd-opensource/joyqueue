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
package org.joyqueue.broker.mqtt.test;

import org.joyqueue.toolkit.network.IpUtil;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

/**
 * @author majun8
 */
public class MqttClientPahoTest {
    private static final Logger logger = LoggerFactory.getLogger(MqttClientPahoTest.class);

    private static String TOPIC = "test_topic";
    private static int TOPIC_COUNT = 5;
    private static String ipAddress = IpUtil.getLocalIp();
    private static String address = String.format("tcp://%s:50088", ipAddress);
    private static String username = "test_app";
    private static String password = "test_token";
    private static String constantClientId = "test_app";
    private static MqttClient client;
    private static MqttConnectOptions connectOptions = new MqttConnectOptions();

    private AtomicInteger counter = new AtomicInteger(1);
    private CountDownLatch latch = new CountDownLatch(1);

    static {
        try {
            //String clientId = constantClientId + "-" + UUID.randomUUID().toString().replace("-", "");
            String clientId = constantClientId;
            client = new MqttClient(address, clientId, new MemoryPersistence());
            client.setTimeToWait(1000);

            connectOptions.setCleanSession(false);
            connectOptions.setUserName(username);
            connectOptions.setPassword(password.toCharArray());
            connectOptions.setConnectionTimeout(30);
            connectOptions.setKeepAliveInterval(60);
            connectOptions.setMqttVersion(MQTT_VERSION_3_1_1);
            connectOptions.setMaxInflight(100);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void connect() {
        client.setCallback(new MqttCallback() {

            public void connectionLost(Throwable cause) {
                logger.error("connectionLost-----------: <{}>", cause);
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.info("deliveryComplete-----------: <{}>", (Object) token.getTopics());
            }

            public void messageArrived(String topic, MqttMessage arg1)
                    throws Exception {
                logger.info("messageArrived-----------: <{}>, <{}>", topic, arg1.toString());
                if (counter.getAndIncrement() == 5) {
                    latch.countDown();
                }
            }
        });

        try {
            client.connect(connectOptions);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void testPublish() throws MqttException {
        String seedString = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < TOPIC_COUNT; i++) {
            String topic = TOPIC + "_" + i;
            StringBuilder sb = new StringBuilder();
            int len = seedString.length();
            for (int id = 0; id < len; id++) {
                sb.append(seedString.charAt((int) Math.round(Math.random() * (len-1))));
            }

            String messagebody = sb.toString();
            logger.info("send topic: <{}>, messagebody is <{}>", topic, messagebody);
            MqttMessage message = new MqttMessage();
            message.setQos(1);
            message.setPayload(messagebody.getBytes(Charset.forName("UTF-8")));
            client.publish(topic, message);
        }
    }

    public void testSubscribe() throws MqttException {
        for (int i = 0; i < TOPIC_COUNT; i++) {
            client.subscribe(TOPIC + "_" + i, 1);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testUnSubscribe() throws MqttException {
        for (int i = 0; i < TOPIC_COUNT; i++) {
            client.unsubscribe(TOPIC + "_" + i);
        }
    }

    public void disconnect() throws MqttException {
        client.disconnect();
        client.close();
    }

    public static void main(String args[]) {
        MqttClientPahoTest mqttClientTest = new MqttClientPahoTest();
        mqttClientTest.connect();
        try {
            mqttClientTest.testPublish();
            mqttClientTest.testSubscribe();
            mqttClientTest.testUnSubscribe();
            Thread.sleep(3000);
            mqttClientTest.disconnect();
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
