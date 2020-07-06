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
//package org.joyqueue.broker.kafka;
//
//import com.google.common.collect.Maps;
//import org.joyqueue.broker.kafka.conf.KafkaConfigs;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.apache.commons.lang3.RandomUtils;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.clients.consumer.OffsetAndMetadata;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.common.TopicPartition;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//
//import java.util.Arrays;
//import java.util.Map;
//import java.util.Properties;
//
///**
// *
// * author: gaohaoxiang
// * date: 2019/3/22
// */
//public class TransactionKafkaProducer {
//
//    public static void main(String[] args) throws Exception {
//        Properties consumerProperties = new Properties();
//        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
//        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfigs.GROUP_ID);
//        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConfigs.GROUP_ID);
//        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//
//        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
//        consumer.subscribe(Arrays.asList("test_topic_0"));
//
//        Properties producerProperties = new Properties();
//        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
//        producerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConfigs.CLIENT_ID);
//        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        producerProperties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, KafkaConfigs.TRANSACTION_ID);
//        producerProperties.put(ProducerConfig.RETRIES_CONFIG, 10);
//        producerProperties.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 1000 * 5);
//        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(producerProperties);
//        kafkaProducer.initTransactions();
//
//        while (true) {
//            try {
//                ConsumerRecords<String, String> records = consumer.poll(1000 * 1);
//
//                Map<TopicPartition, OffsetAndMetadata> offsetMap = Maps.newHashMap();
//                kafkaProducer.beginTransaction();
//
//                for (ConsumerRecord<String, String> record : records) {
//                    System.out.println("record: " + record.topic() + "_" + record.partition() + "_" + record.offset());
//                    offsetMap.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset(), null));
//
//                    for (int j = 0; j < 10; j++) {
//                        kafkaProducer.send(new ProducerRecord<String, String>("test_topic_1", RandomStringUtils.random(RandomUtils.nextInt(10, 100)))).get();
//                    }
//                }
//
//                kafkaProducer.sendOffsetsToTransaction(offsetMap, KafkaConfigs.GROUP_ID);
//                kafkaProducer.commitTransaction();
//
//                System.out.println("commit");
//                Thread.currentThread().sleep(1000 *1);
//            } catch (Exception e) {
//                e.printStackTrace();
//                kafkaProducer.close();
//                kafkaProducer = new KafkaProducer<>(producerProperties);
//                kafkaProducer.initTransactions();
//            }
//        }
//    }
//}