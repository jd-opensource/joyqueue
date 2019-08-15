package io.chubao.joyqueue.broker.kafka;

import io.chubao.joyqueue.broker.kafka.conf.KafkaConfigs;
import io.chubao.joyqueue.broker.kafka.producer.Producer;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * KafkaTest
 *
 * @author luoruiheng
 * @since 2/24/17
 */
public class KafkaProduceTest {

    public static void main(String[] args) throws Exception {
        boolean isAsync = false;
        Producer producer = new Producer(KafkaConfigs.TOPIC, isAsync);
        String value = "";
        for (int i = 0; i < 1024; i++) {
            value += "a";
        }

        System.out.println("kafka producer is started");

        for (int i = 0; i < 10000; i++) {
            long startTime = SystemClock.now();
            //        for (int i = 0; i < 10000 * 10 * 10; i++) {
            for (int j = 0; j < 10000; j++) {
                try {
                    Thread.sleep(1);
                    producer.sendAsync(new ProducerRecord(KafkaConfigs.TOPIC, "TEST", value));
//                  RecordMetadata record = producer.send(new ProducerRecord(KafkaConfigs.TOPIC, 1, (long) i, "test_" + j, "test_" + j));
//                    RecordMetadata record = producer.send(new ProducerRecord(KafkaConfigs.TOPIC + "_" + (j % KafkaConfigs.TOPIC_COUNT),"test_" + j, "test_" + j));
//                    System.out.println(String.format("sendResult, topic: %s, partition: %s, offset: %s, index : %s", record.topic(), record.partition(), record.offset(), i));
//                  Thread.currentThread().sleep(1000 * 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(1000);

            System.out.println(SystemClock.now() - startTime);
        }

        System.in.read();
    }
}