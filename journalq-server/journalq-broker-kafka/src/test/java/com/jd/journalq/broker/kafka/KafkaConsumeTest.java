package com.jd.journalq.broker.kafka;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.kafka.conf.KafkaConfigs;
import com.jd.journalq.broker.kafka.consumer.Consumer;
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