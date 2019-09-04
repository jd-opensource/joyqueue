package io.chubao.joyqueue.nsr.message.support.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * MessengerHeartbeatRequest
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerHeartbeatRequest extends JoyQueuePayload implements Type {

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_HEARTBEAT_REQUEST;
    }
}
