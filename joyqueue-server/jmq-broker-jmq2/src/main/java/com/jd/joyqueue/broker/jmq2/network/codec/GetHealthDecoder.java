/**
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
package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.command.GetHealth;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import io.netty.buffer.ByteBuf;

public class GetHealthDecoder {

    public GetHealth decode(final GetHealth payload, final ByteBuf in) throws Exception {
        // 应用
        payload.setApp(Serializer.readString(in));
        //主题
        payload.setTopic(Serializer.readString(in));
        // 1字节数据中心
        payload.setDataCenter(in.readByte());
        return payload;
    }
}