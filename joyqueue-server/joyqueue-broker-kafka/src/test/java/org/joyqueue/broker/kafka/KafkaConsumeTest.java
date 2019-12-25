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
package org.joyqueue.broker.kafka;

import com.google.common.collect.Lists;
import org.joyqueue.broker.kafka.conf.KafkaConfigs;
import org.joyqueue.broker.kafka.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.List;

/**
 * KafkaTest
 *
 * @author luoruiheng
 * @since 2/24/17
 */
public class KafkaConsumeTest {

    public static void main(String[] args) {
        Consumer consumer = new Consumer(KafkaConfigs.TOPIC);
        KafkaConsumer kafkaConsumer = consumer.getKafkaConsumer();

        List<String> topics = Lists.newLinkedList();
        for (int i = 0; i < KafkaConfigs.TOPIC_COUNT; i++) {
            topics.add(KafkaConfigs.TOPIC + "_" + i);
        }

        List<TopicPartition> topicPartitions = Lists.newLinkedList();
        for (Object partitionInfo : kafkaConsumer.partitionsFor(KafkaConfigs.TOPIC)) {
            topicPartitions.add(new TopicPartition(((PartitionInfo) partitionInfo).topic(), ((PartitionInfo) partitionInfo).partition()));
        }

//        kafkaConsumer.assign(topicPartitions);
        kafkaConsumer.subscribe(Lists.newArrayList(KafkaConfigs.TOPIC));

        System.out.println("kafka consumer is started");

        while (!Thread.interrupted()) {
            consumer.doWork();
        }
    }

}