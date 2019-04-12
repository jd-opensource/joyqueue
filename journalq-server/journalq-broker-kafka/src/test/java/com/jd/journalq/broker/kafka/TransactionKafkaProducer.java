package com.jd.journalq.broker.kafka;

import com.jd.journalq.broker.kafka.conf.KafkaConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/22
 */
public class TransactionKafkaProducer {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConfigs.CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, KafkaConfigs.TRANSACTION_ID);
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);
        kafkaProducer.initTransactions();

        kafkaProducer.beginTransaction();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5; j++) {
                try {
                    kafkaProducer.send(new ProducerRecord<String, String>("test_topic_0", "test", "test")).get();
                } catch (Exception e) {
//                    kafkaProducer.initTransactions();
                    e.printStackTrace();
                }

                System.out.println("send");
                Thread.currentThread().sleep(1000 * 1);
            }
        }
        kafkaProducer.commitTransaction();
    }
}