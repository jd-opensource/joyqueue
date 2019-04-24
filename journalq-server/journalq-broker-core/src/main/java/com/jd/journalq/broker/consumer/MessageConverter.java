package com.jd.journalq.broker.consumer;

import com.jd.journalq.message.BrokerMessage;
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