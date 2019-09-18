package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.AllMetadata;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * GetAllMetadataResponse
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class GetAllMetadataResponse extends JoyQueuePayload {

    private AllMetadata metadata;
    private byte[] response;

    public void setMetadata(AllMetadata metadata) {
        this.metadata = metadata;
    }

    public AllMetadata getMetadata() {
        return metadata;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public byte[] getResponse() {
        return response;
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_GET_ALL_METADATA_RESPONSE;
    }
}