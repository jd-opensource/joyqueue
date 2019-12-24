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
package org.joyqueue.broker.election.network.codec;

import org.joyqueue.broker.election.command.AppendEntriesRequest;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/27
 */
public class AppendEntriesRequestEncoder implements PayloadEncoder<AppendEntriesRequest>, Type {
    @Override
    public void encode(final AppendEntriesRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getPartitionGroup());
        buffer.writeInt(payload.getTerm());
        buffer.writeInt(payload.getLeaderId());

        buffer.writeInt(payload.getPrevTerm());
        buffer.writeLong(payload.getPrevPosition());

        buffer.writeLong(payload.getStartPosition());
        buffer.writeLong(payload.getCommitPosition());
        buffer.writeLong(payload.getLeftPosition());

        buffer.writeBoolean(payload.isMatch());

        ByteBuffer entries = payload.getEntries();
        if (entries == null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(entries.remaining());
        entries.mark();
        buffer.writeBytes(entries);
        entries.reset();
    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_REQUEST;
    }
}
