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

import org.joyqueue.domain.Config;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetAllConfigsAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetAllConfigsAckCodec implements NsrPayloadCodec<GetAllConfigsAck>, Type {
    @Override
    public GetAllConfigsAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAllConfigsAck allConfigsAck = new GetAllConfigsAck();
            // 1.int
            int size = buffer.readInt();
            List<Config> list = new ArrayList<>(size);
            for(int i = 0 ;i<size;i++){
                Config config = new Config();
                //3.String(1)
                config.setGroup(Serializer.readString(buffer));
                //4.String(1)
                config.setKey(Serializer.readString(buffer));
                //5.String(1)
                config.setValue(Serializer.readString(buffer));
                list.add(config);
            }
            allConfigsAck.configs(list);
        return allConfigsAck;
    }

    @Override
    public void encode(GetAllConfigsAck payload, ByteBuf buffer) throws Exception {
        List<Config> configList = payload.getConfigs();
        if(null==configList||configList.size()<0){
            buffer.writeInt(0);
            return;
        }
        //2.int
        buffer.writeInt(configList.size());
        for(Config config : configList){
            //3.String(1)
            Serializer.write(config.getGroup(),buffer);
            //4.String(1)
            Serializer.write(config.getKey(),buffer);
            //5.String(1)
            Serializer.write(config.getValue(),buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_CONFIG_ACK;
    }
}
