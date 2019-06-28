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
package com.jd.joyqueue.nsr.network.codec;

import com.jd.joyqueue.network.transport.command.Header;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.nsr.network.NsrPayloadCodec;
import com.jd.joyqueue.nsr.network.command.NsrCommandType;
import com.jd.joyqueue.nsr.network.command.NsrConnection;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/3/15
 */
public class ConnectionCodec implements NsrPayloadCodec<NsrConnection>, Type {
    @Override
    public NsrConnection decode(Header header, ByteBuf buffer) throws Exception {
        return new NsrConnection().brokerId(buffer.readInt());
    }

    @Override
    public void encode(NsrConnection nsrConnection, ByteBuf buffer) throws Exception {
        buffer.writeInt(nsrConnection.getBrokerId());
    }

    @Override
    public int type() {
        return NsrCommandType.CONNECT;
    }
}
