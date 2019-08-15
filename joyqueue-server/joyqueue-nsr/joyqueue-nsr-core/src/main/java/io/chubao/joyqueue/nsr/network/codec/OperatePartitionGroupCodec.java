package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.PartitionGroup;

import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.CreatePartitionGroup;
import io.chubao.joyqueue.nsr.network.command.RemovePartitionGroup;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.OperatePartitionGroup;
import io.chubao.joyqueue.nsr.network.command.UpdatePartitionGroup;
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
        Serializer.write(partitionGroup, buffer);
        buffer.writeBoolean(payload.isRollback());
    }
}
