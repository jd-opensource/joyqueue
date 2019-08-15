package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * HeartbeatRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class HeartbeatRequest extends JoyQueuePayload {

    @Override
    public int type() {
        return JoyQueueCommandType.HEARTBEAT_REQUEST.getCode();
    }
}