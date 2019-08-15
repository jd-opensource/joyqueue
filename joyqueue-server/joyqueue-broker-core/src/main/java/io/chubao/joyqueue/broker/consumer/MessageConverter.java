package io.chubao.joyqueue.broker.consumer;

import io.chubao.joyqueue.message.BrokerMessage;
import com.jd.laf.extension.Type;

import java.util.List;

/**
 * MessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/3
 */
public interface MessageConverter extends Type<Byte> {

    BrokerMessage convert(BrokerMessage message);

    List<BrokerMessage> convertBatch(BrokerMessage message);

    byte target();
}