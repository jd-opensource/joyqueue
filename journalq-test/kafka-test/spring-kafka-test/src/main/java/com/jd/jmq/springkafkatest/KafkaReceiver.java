package com.jd.journalq.springkafkatest;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaReceiver {
    private static Logger logger = LoggerFactory.getLogger(KafkaReceiver.class);

    @KafkaListener(topics = {"default.topic1"})
    public void listen(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            logger.info("Receive message, partition:{}, key:{}, value:{}",
                    record.partition(), record.key(), record.value());
        }
    }
}
