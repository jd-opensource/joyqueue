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
package org.joyqueue.client.samples.springcloud.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

@EnableBinding(CustomProcessor.class)
@SpringBootApplication(scanBasePackages = {"org.joyqueue.client.samples.springcloud.stream"})
public class StreamBootstrap {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("spring.profiles.active", "stream");
        ConfigurableApplicationContext run = SpringApplication.run(StreamBootstrap.class, args);
        CustomProcessor processor = run.getBean(CustomProcessor.class);

        for (int i = 0; i < 100; i++) {
            Message<String> message = new GenericMessage<>("Hello - " + i);
            //Message<String> received = (Message<String>) messageCollector.forChannel(processor.output()).poll();
            //Assert.assertThat(received.getPayload(), equalTo("hello world"));
            processor.outputOrder().send(message);

            Thread.sleep(1000);
        }
    }
}
