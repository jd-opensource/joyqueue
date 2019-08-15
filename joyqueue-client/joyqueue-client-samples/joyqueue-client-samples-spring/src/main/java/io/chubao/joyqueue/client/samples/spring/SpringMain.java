package io.chubao.joyqueue.client.samples.spring;

import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * author: gaohaoxiang
 * date: 2019/3/6
 */
public class SpringMain {

    protected static final Logger logger = LoggerFactory.getLogger(SpringMain.class);

    public static void main(String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-sample.xml");
        Producer producer = (Producer) applicationContext.getBean("producer1");

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage("test_topic_0", "test".getBytes());
            SendResult sendResult = producer.send(message);
            logger.info("Message ID: {}", sendResult.messageId());
        }
    }
}