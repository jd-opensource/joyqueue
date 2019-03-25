package com.jd.journalq.network.protocol;

import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;

/**
 * 协议
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface Protocol {

    CodecFactory createCodecFactory();

    CommandHandlerFactory createCommandHandlerFactory();

    String type();
}