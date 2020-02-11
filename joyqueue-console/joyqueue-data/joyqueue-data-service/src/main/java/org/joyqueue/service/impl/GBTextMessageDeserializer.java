package org.joyqueue.service.impl;

import org.joyqueue.service.MessageDeserializer;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author LiYue
 * Date: 2020/2/8
 */
@Component
public class GBTextMessageDeserializer implements MessageDeserializer {
    @Override
    public String getSerializeTypeName() {
        return "GB18030/GBK/GB2312 TEXT";
    }

    @Override
    public String deserialize(byte[] binaryMessage) {
        if(binaryMessage == null) throw new NullPointerException();
        return new String(binaryMessage, Charset.forName("GB18030"));
    }
}
