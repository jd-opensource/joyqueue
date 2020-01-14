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

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Types;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.CreatePartitionGroup;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.nsr.network.command.OperatePartitionGroup;
import org.joyqueue.nsr.network.command.RemovePartitionGroup;
import org.joyqueue.nsr.network.command.UpdatePartitionGroup;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2018/10/11
 */
public class OperatePartitionGroupCodec implements NsrPayloadCodec<OperatePartitionGroup>, Types {
    private static final int[] types = new int[]{NsrCommandType.NSR_CREATE_PARTITIONGROUP,
            NsrCommandType.NSR_UPDATE_PARTITIONGROUP,
            NsrCommandType.NSR_REMOVE_PARTITIONGROUP,
            NsrCommandType.NSR_LEADERCHANAGE_PARTITIONGROUP};

    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        PartitionGroup group = Serializer.readPartitionGroup(buffer, header.getVersion());
        boolean rollback = buffer.readBoolean();
        int cmdType = header.getType();
        if (cmdType == NsrCommandType.NSR_CREATE_PARTITIONGROUP) {
            return new CreatePartitionGroup(group, rollback);
        } else if (cmdType == NsrCommandType.NSR_UPDATE_PARTITIONGROUP || cmdType == NsrCommandType.NSR_LEADERCHANAGE_PARTITIONGROUP) {
            return new UpdatePartitionGroup(group, rollback);
        } else if (cmdType == NsrCommandType.NSR_REMOVE_PARTITIONGROUP) {
            return new RemovePartitionGroup(group, rollback);
        } else if (cmdType == NsrCommandType.NSR_LEADERCHANAGE_PARTITIONGROUP) {
            return new UpdatePartitionGroup(group,rollback);
        }
        return null;
    }

    @Override
    public int[] types() {
        return types;
    }

    @Override
    public void encode(OperatePartitionGroup payload, ByteBuf buffer) throws Exception {
        PartitionGroup partitionGroup = payload.getPartitionGroup();
        Serializer.write(partitionGroup, buffer, payload.getHeader().getVersion());
        buffer.writeBoolean(payload.isRollback());
    }
}
