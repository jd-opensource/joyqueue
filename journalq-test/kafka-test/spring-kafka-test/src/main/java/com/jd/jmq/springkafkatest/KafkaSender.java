/**
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
