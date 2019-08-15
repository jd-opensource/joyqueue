package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetAllConfigs extends JoyQueuePayload {
    @Override
    public int type() {
        return NsrCommandType.GET_ALL_CONFIG;
    }
}
