/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.client.samples.spring;

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