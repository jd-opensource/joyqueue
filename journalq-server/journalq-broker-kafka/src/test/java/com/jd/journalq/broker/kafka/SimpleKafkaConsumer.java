package com.jd.journalq.broker.kafka;

import com.jd.journalq.broker.kafka.conf.KafkaConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.requests.IsolationLevel;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/22
 */
public class SimpleKafkaConsumer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfigs.GROUP_ID);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConfigs.GROUP_ID);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("test_topic_0"));

        while (true) {
//            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000 * 1));
            ConsumerRecords<String, String> records = consumer.poll(1000 * 1);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(String.format("record, key: %s, value: %s, offset: %s, timestamp: %s", record.key(), record.value(), record.offset(), record.timestamp()));
            }
        }
    }
}