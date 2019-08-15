package io.chubao.joyqueue.network.protocol;

import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;

/**
 * Protocol
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public interface Protocol {

    CodecFactory createCodecFactory();

    CommandHandlerFactory createCommandHandlerFactory();

    String type();
}