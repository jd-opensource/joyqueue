package io.chubao.joyqueue.nsr.network.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.chubao.joyqueue.domain.AllMetadata;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetAllMetadataResponse;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.toolkit.io.ZipUtil;
import io.netty.buffer.ByteBuf;

/**
 * GetAllMetadataResponseCodec
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class GetAllMetadataResponseCodec implements NsrPayloadCodec<GetAllMetadataResponse>, Type {

    @Override
    public GetAllMetadataResponse decode(Header header, ByteBuf buffer) throws Exception {
        int length = buffer.readInt();
        byte[] json = new byte[length];
        buffer.readBytes(json);

        GetAllMetadataResponse allMetadataResponse = new GetAllMetadataResponse();
        allMetadataResponse.setMetadata((AllMetadata) parseJson(json, AllMetadata.class));
        return allMetadataResponse;
    }

    @Override
    public void encode(GetAllMetadataResponse payload, ByteBuf buffer) throws Exception {
        byte[] json = null;
        if (payload.getResponse() != null) {
            json = payload.getResponse();
        } else {
            json = toJson(payload);
        }
        buffer.writeInt(json.length);
        buffer.writeBytes(json);
    }

    // TODO 抛runtime不太合适
    public static Object parseJson(byte[] json, Class<?> type) {
        try {
            return JSON.parseObject(ZipUtil.decompress(json), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toJson(Object value) {
        try {
            String json = JSON.toJSONString(value, SerializerFeature.DisableCircularReferenceDetect);
            return ZipUtil.compress(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_GET_ALL_METADATA_RESPONSE;
    }
}