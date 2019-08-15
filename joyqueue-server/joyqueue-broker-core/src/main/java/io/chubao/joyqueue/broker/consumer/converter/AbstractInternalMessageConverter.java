package io.chubao.joyqueue.broker.consumer.converter;

import io.chubao.joyqueue.broker.consumer.MessageConverter;
import io.chubao.joyqueue.message.SourceType;

/**
 * AbstractInternalMessageConverter
 *
 * author: gaohaoxiang
 * date: 2019/4/24
 */
public abstract class AbstractInternalMessageConverter implements MessageConverter {

    @Override
    public byte target() {
        return SourceType.INTERNAL.getValue();
    }
}