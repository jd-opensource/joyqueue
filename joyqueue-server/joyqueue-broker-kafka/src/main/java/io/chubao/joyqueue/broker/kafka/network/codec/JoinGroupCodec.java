package io.chubao.joyqueue.broker.kafka.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.JoinGroupRequest;
import io.chubao.joyqueue.broker.kafka.command.JoinGroupResponse;
import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * JoinGroupCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class JoinGroupCodec implements KafkaPayloadCodec<JoinGroupResponse>, Type {

    @Override
    public JoinGroupRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        JoinGroupRequest request = new JoinGroupRequest();
        List<JoinGroupRequest.ProtocolMetadata> groupProtocols = null;

        request.setGroupId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        request.setSessionTimeout(buffer.readInt());

        if (header.getVersion() >= 1) {
            request.setRebalanceTimeout(buffer.readInt());
        }

        String memberId = StringUtils.defaultString(Serializer.readString(buffer, Serializer.SHORT_SIZE), StringUtils.EMPTY);
        request.setMemberId(memberId);
        request.setProtocolType(Serializer.readString(buffer, Serializer.SHORT_SIZE));

        int size = buffer.readInt();

        if (size > 0) {
            groupProtocols = Lists.newLinkedList();
        }
        for (int i = 0; i < size; i++) {
            String groupName = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int length = buffer.readInt();
            ByteBuf byteBuf = buffer.readBytes(length);
            byte[] bytes;
            if (byteBuf.hasArray()) {
                bytes = byteBuf.array();
            } else {
                bytes = new byte[length];
                byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            }
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.rewind();
            JoinGroupRequest.ProtocolMetadata protocolMetadata = new JoinGroupRequest.ProtocolMetadata(groupName, byteBuffer);
            groupProtocols.add(protocolMetadata);
        }

        request.setGroupProtocols(groupProtocols);
        return request;
    }

    @Override
    public void encode(JoinGroupResponse payload, ByteBuf buffer) throws Exception {
        JoinGroupResponse response = payload;
        if (response.getVersion() >= 2) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        // 错误码
        buffer.writeShort(response.getErrorCode());
        buffer.writeInt(response.getGenerationId());
        try {
            Serializer.write(response.getGroupProtocol(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(response.getLeaderId(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(response.getMemberId(), buffer, Serializer.SHORT_SIZE);
        } catch (Exception e) {
            throw new TransportException.CodecException(e);
        }
        Map<String, ByteBuffer> members = response.getMembers();
        if (members != null) {
            int size = members.size();
            buffer.writeInt(size);
            for (Map.Entry<String, ByteBuffer> entry : members.entrySet()) {
                try {
                    Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
                } catch (Exception e) {
                    throw new TransportException.CodecException(e);
                }
                ByteBuffer arg = entry.getValue();
                int pos = arg.position();
                buffer.writeInt(arg.remaining());
                buffer.writeBytes(arg);
                arg.position(pos);
            }
        } else {
            buffer.writeInt(0);
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.JOIN_GROUP.getCode();
    }
}