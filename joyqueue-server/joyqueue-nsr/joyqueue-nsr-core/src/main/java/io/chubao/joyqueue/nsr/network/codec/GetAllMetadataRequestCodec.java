package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetAllMetadataRequest;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * GetAllMetadataRequestCodec
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class GetAllMetadataRequestCodec implements NsrPayloadCodec<GetAllMetadataRequest>, Type {

    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        return new GetAllMetadataRequest();
    }

    @Override
    public void encode(GetAllMetadataRequest payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return NsrCommandType.NSR_GET_ALL_METADATA_REQUEST;
    }
}