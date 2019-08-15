package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * RemoveConnectionRequest
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class RemoveConnectionRequest extends JoyQueuePayload {

    @Override
    public int type() {
        return JoyQueueCommandType.REMOVE_CONNECTION_REQUEST.getCode();
    }
}