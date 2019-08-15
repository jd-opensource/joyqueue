package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchHealthRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class FetchHealthRequest extends JoyQueuePayload {

    @Override
    public int type() {
        return JoyQueueCommandType.HEARTBEAT_REQUEST.getCode();
    }
}