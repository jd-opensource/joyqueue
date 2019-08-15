package io.chubao.joyqueue.client.internal.consumer.converter;

import io.chubao.joyqueue.message.BrokerMessage;
import com.jd.laf.extension.Type;

import java.util.List;

/**
 * MessageConverter
 *
 * author: gaohaoxiang
 * date: 2019/4/3
 */
public interface MessageConverter extends Type<Byte> {

    BrokerMessage convert(BrokerMessage message);

    List<BrokerMessage> convertBatch(BrokerMessage message);
}