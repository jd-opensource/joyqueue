package com.jd.journalq.springkafkatest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;


@Component
public class KafkaSender {
    private static Logger logger = LoggerFactory.getLogger(KafkaSender.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(Integer partition, String key, String value) {
        try {
            SendResult result = kafkaTemplate.send("default.topic1", partition, key, value).get();
            logger.info("Send message(partition:{},key:{},value:{}) success, partition is {}, offset is {}",
                    partition, key, value, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
