/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.network.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.joyqueue.domain.AllMetadata;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetAllMetadataResponse;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.toolkit.io.ZipUtil;
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