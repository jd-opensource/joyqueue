package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * GetAllMetadataRequest
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class GetAllMetadataRequest extends JoyQueuePayload {

    @Override
    public int type() {
        return NsrCommandType.NSR_GET_ALL_METADATA_REQUEST;
    }
}