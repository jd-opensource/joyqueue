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
package org.joyqueue.tools;

import com.beust.jcommander.JCommander;
import org.joyqueue.tools.config.ConsoleProducerConfig;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueNameServerBuiltinKeys;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ConsoleProducer
 *
 * author: gaohaoxiang
 * date: 2019/6/26
 */
public class ConsoleProducer {

    protected static Logger logger = LoggerFactory.getLogger(ConsoleProducer.class);

    public static void main(String[] args) {
        ConsoleProducerConfig config = new ConsoleProducerConfig();
        JCommander jcommander = JCommander.newBuilder()
                .addObject(config)
                .build();
        jcommander.parse(args);

        if (config.isHelp()) {
            jcommander.usage();
            return;
        }

        Producer producer = buildProducer(config);
        producer.start();

        send(producer, config);
        System.exit(0);
    }

    protected static Producer buildProducer(ConsoleProducerConfig config) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, config.getToken());
        keyValue.put(JoyQueueNameServerBuiltinKeys.NAMESPACE, StringUtils.defaultIfBlank(config.getNamespace(), ToolConsts.DEFAULT_NAMESPACE));

        for (Map.Entry<String, String> entry : config.getParams().entrySet()) {
            keyValue.put(entry.getKey(), entry.getValue());
        }

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(
                String.format("oms:%s://%s@%s/%s",
                        ToolConsts.DRIVER, config.getApp(), config.getBootstrap(), StringUtils.defaultIfBlank(config.getRegion(), ToolConsts.DEFAULT_REGION)), keyValue);
        return messagingAccessPoint.createProducer();
    }

    protected static void send(Producer producer, ConsoleProducerConfig config) {
        Message message = producer.createMessage(config.getTopic(), StringUtils.defaultString(config.getBody(), ToolConsts.DEFAULT_BODY).getBytes());

        if (StringUtils.isNotBlank(config.getKey())) {
            message.extensionHeader().get().setMessageKey(config.getKey());
        }

        producer.send(message);

        producer.stop();
    }
}