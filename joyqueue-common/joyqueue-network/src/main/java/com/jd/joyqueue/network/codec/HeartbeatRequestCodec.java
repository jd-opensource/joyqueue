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
package com.jd.joyqueue.network.codec;

import com.jd.joyqueue.network.command.HeartbeatRequest;
import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.transport.codec.JournalqHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * HeartbeatRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class HeartbeatRequestCodec implements PayloadCodec<JournalqHeader, HeartbeatRequest>, Type {

    @Override
    public HeartbeatRequest decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        return new HeartbeatRequest();
    }

    @Override
    public void encode(HeartbeatRequest payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JournalqCommandType.HEARTBEAT_REQUEST.getCode();
    }
}