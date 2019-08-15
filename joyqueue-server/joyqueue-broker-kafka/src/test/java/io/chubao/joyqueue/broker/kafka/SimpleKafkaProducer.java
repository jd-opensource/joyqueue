package io.chubao.joyqueue.broker.kafka;

import io.chubao.joyqueue.broker.kafka.conf.KafkaConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 *
 * author: gaohaoxiang
 * date: 2019/3/22
 */
public class SimpleKafkaProducer {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConfigs.CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

        while (true) {
            kafkaProducer.send(new ProducerRecord<String, String>("test_topic_0", "kafka-test","test")).get();
            System.out.println("send");
            Thread.currentThread().sleep(1000 * 1);
        }
    }
}