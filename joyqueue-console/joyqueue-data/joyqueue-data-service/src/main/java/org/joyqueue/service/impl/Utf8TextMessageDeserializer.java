package org.joyqueue.service.impl;

import org.joyqueue.service.MessageDeserializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author LiYue
 * Date: 2020/2/8
 */
@Component(value = "defaultMessageDeserializer")
public class Utf8TextMessageDeserializer implements MessageDeserializer {
    @Override
    public String getSerializeTypeName() {
        return "UTF-8 TEXT";
    }

    @Override
    public String deserialize(byte[] binaryMessage) {
        if(binaryMessage == null) throw new NullPointerException();
        return new String(binaryMessage, StandardCharsets.UTF_8);
    }
}
