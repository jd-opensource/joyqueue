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

import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.broker.election.command.TimeoutNowRequest;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/2
 */
public class TimeoutNowRequestEncoder implements PayloadEncoder<TimeoutNowRequest>, Type {
    @Override
    public void encode(final TimeoutNowRequest request, ByteBuf buffer) throws Exception {
        Serializer.write(request.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(request.getPartitionGroup());
        buffer.writeInt(request.getTerm());
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
