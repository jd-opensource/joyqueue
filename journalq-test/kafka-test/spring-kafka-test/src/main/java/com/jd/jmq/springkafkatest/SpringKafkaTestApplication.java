package com.jd.journalq.springkafkatest;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringKafkaTestApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringKafkaTestApplication.class, args);

        KafkaSender kafkaSender = context.getBean(KafkaSender.class);
        for (int i = 0; i < 100; i++) {
            kafkaSender.send(1, "test_key_" + i, "test_value" + i);
        }

        for (int i = 100; i < 200; i++) {
            kafkaSender.send(null, "test_key_" + i, "test_value" + i);
        }

        for (int i = 200; i < 300; i++) {
            kafkaSender.send(null, null, "test_value" + i);
        }
    }

}

