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

import org.joyqueue.domain.DataCenter;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetDataCenterAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetDataCenterAckCodec implements NsrPayloadCodec<GetDataCenterAck>, Type {
    @Override
    public GetDataCenterAck decode(Header header, ByteBuf buffer) throws Exception {
        DataCenter dataCenter = new DataCenter();
        dataCenter.setCode(Serializer.readString(buffer));
        dataCenter.setName(Serializer.readString(buffer));
        dataCenter.setRegion(Serializer.readString(buffer));
        dataCenter.setUrl(Serializer.readString(buffer));
        return new GetDataCenterAck().dataCenter(dataCenter);
    }

    @Override
    public void encode(GetDataCenterAck payload, ByteBuf buffer) throws Exception {
        DataCenter dataCenter = payload.getDataCenter();
        Serializer.write(dataCenter.getCode(),buffer);
        Serializer.write(dataCenter.getName(),buffer);
        Serializer.write(dataCenter.getRegion(),buffer);
        Serializer.write(dataCenter.getUrl(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_DATACENTER_ACK;
    }
}
