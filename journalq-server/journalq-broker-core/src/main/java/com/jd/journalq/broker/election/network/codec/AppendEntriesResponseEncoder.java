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
package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.command.AppendEntriesResponse;
import com.jd.journalq.network.transport.codec.PayloadEncoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesResponseEncoder implements PayloadEncoder<AppendEntriesResponse>, Type {
    @Override
    public void encode(final AppendEntriesResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getTerm());
        buffer.writeBoolean(payload.isSuccess());
        buffer.writeLong(payload.getNextPosition());
        buffer.writeLong(payload.getWritePosition());
        buffer.writeInt(payload.getReplicaId());
    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_RESPONSE;
    }
}
