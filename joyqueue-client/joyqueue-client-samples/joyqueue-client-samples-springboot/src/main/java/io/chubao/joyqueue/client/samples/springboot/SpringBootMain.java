package io.chubao.joyqueue.client.samples.springboot;

import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;

/**
 * SpringBootMain
 *
 * author: gaohaoxiang
 * date: 2019/2/22
 */
@SpringBootApplication
@ComponentScan("io.chubao.joyqueue.client.samples.springboot")
public class SpringBootMain implements InitializingBean {

    protected static final Logger logger = LoggerFactory.getLogger(SpringBootMain.class);

    @Resource
    private Producer producer;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMain.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage("test_topic_0", "test".getBytes());
            SendResult sendResult = producer.send(message);
            logger.info("sendResult: {}", sendResult);
        }
    }
}