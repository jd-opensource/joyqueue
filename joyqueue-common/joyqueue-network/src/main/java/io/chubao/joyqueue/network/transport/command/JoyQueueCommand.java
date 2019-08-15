package io.chubao.joyqueue.network.transport.command;

import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;

/**
 * JoyQueueCommand
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class JoyQueueCommand extends Command {

    public JoyQueueCommand(JoyQueuePayload payload) {
        this(payload.type(), payload);
    }

    public JoyQueueCommand(int type, JoyQueuePayload payload) {
        setHeader(new JoyQueueHeader(type));
        setPayload(payload);
    }

    @Override
    public String toString() {
        return header.toString();
    }
}