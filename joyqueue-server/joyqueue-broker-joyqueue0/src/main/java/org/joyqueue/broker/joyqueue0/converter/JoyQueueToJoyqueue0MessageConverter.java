package org.joyqueue.broker.joyqueue0.converter;

import org.joyqueue.broker.consumer.converter.JoyQueueToInternalMessageConverter;
import org.joyqueue.message.SourceType;

/**
 * JoyQueueToJmq2MessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/24
 */
public class JoyQueueToJoyqueue0MessageConverter extends JoyQueueToInternalMessageConverter {

    @Override
    public byte target() {
        return SourceType.JOYQUEUE0.getValue();
    }
}