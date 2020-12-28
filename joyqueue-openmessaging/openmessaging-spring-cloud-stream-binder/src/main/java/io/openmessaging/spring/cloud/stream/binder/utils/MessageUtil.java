/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.spring.cloud.stream.binder.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openmessaging.producer.Producer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.Charset;

/**
 * Message Util
 */
public class MessageUtil {

    private static final Log log = LogFactory.getLog(MessageUtil.class);

    private static ObjectMapper mapper = new ObjectMapper();

    public static org.springframework.messaging.Message<?> convert2SpringMessage(io.openmessaging.message.Message omsMessage) {
        try {
            return MessageBuilder.withPayload(omsMessage.getData())
                    .copyHeaders(BeanUtils.describe(omsMessage.header()))
                    .build();
        } catch (Exception e) {
            log.error("Convert OMS message to Spring message error! " + e.getMessage(), e);
            throw new MessageConversionException(e.getMessage());
        }
    }

    public static io.openmessaging.message.Message convert2OMSMessage(Producer producer, String destination, org.springframework.messaging.Message<?> message) {
        io.openmessaging.message.Message omsMessage;
        if (message.getPayload() instanceof String) {
            omsMessage = producer.createMessage(destination, message.getPayload().toString().getBytes());
        } else if (message.getPayload() instanceof byte[]) {
            omsMessage = producer.createMessage(destination, (byte[]) message.getPayload());
        } else {
            try {
                String jsonStr = mapper.writeValueAsString(message.getPayload());
                omsMessage = producer.createMessage(destination, jsonStr.getBytes(Charset.forName("utf8")));
            } catch (Exception e) {
                log.error("Convert Spring message to OMS message error! " + e.getMessage(), e);
                throw new RuntimeException("Convert to OMS message failed.", e);
            }
        }
        return omsMessage;
    }
}
