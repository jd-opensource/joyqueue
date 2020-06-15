package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.BrokerCluster;
import com.jd.joyqueue.broker.jmq2.command.BrokerGroup;
import com.jd.joyqueue.broker.jmq2.command.GetClusterAck;
import com.jd.joyqueue.broker.jmq2.command.JMQ2Broker;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * 获取集群应答编码器
 */
public class GetClusterAckCodec implements JMQ2PayloadCodec<GetClusterAck>, Type {

    /**
     * <i>协议格式：</i>
     * <pre>
     *      +----------+----------+       —  +----------+----------+
     *      |  客户端机房(1字节)  |     /    |  主题长度(1字节)    |
     *      +----------+----------+          +----------+----------+
     *      |   条数(2字节)       |   /      |  主题(字节数组)     |
     *      +----------+----------+ -        +----------+----------+       -  +----------+----------+
     *      |   主题集群1         |          |	队列数(1字节)      |    /     |  分组权限(1字节)    |
     *      +----------+----------+          +----------+----------+          +----------+----------+    - - -
     *      +----------+----------+
     *      |   主题集群2         |  \       |	分组数量(1字节)    |  /       |  地址数量(1字节)    |  /       |     地址(6字节)     |
     *      +----------+----------+          +----------+----------+          +----------+----------+
     *      +----------+----------+
     *      |                     |    \     |     分组1           |          |      Broker1        |          |     角色
     *      (1字节)     |
     *      |                     |          +----------+----------+          +----------+----------+
     *      +----------+----------+
     *      |   主题集群n         |      \   |     分组2           |  \       |      Broker2        |  \       |     机房
     *      (4字节)     |
     *      |                     |          +----------+----------+          +----------+----------+    - - -
     *      +----------+----------+
     *      |                     |        \ |     分组n           |    \     |      Brokern        |
     *      +----------+----------+          +----------+----------+       -  +----------+----------+
     * </pre>
     *
     * @param out
     * @throws java.lang.Exception
     */
    @Override
    public void encode(final GetClusterAck payload, final ByteBuf out) throws Exception {
        payload.validate();

        if (payload.getCacheBody() != null) {
            out.writeBytes(payload.getCacheBody());
        } else {
            out.writeByte(payload.getDataCenter());
            out.writeInt(payload.getInterval());
            out.writeInt(payload.getMaxSize());
            List<BrokerCluster> clusters = payload.getClusters();
            int count = clusters == null ? 0 : clusters.size();
            // 2字节条数
            out.writeShort(count);

            for (int i = 0; i < count; i++) {
                BrokerCluster cluster = clusters.get(i);
                List<BrokerGroup> groups = cluster.getGroups();
                // 1字节主题长度
                Serializer.write(cluster.getTopic(), out);
                // 1字节队列数
                out.writeByte(cluster.getQueues());
                // 1字节分组数量
                int groupCount = groups == null ? 0 : groups.size();
                out.writeByte(groupCount);
                for (int j = 0; j < groupCount; j++) {
                    BrokerGroup group = groups.get(j);
                    // 权限
                    out.writeByte(group.getPermission().ordinal());
                    // Broker数据
                    List<JMQ2Broker> brokers = group.getBrokers();
                    int brokerCount = brokers == null ? 0 : brokers.size();
                    // 1字节Broker数量
                    out.writeByte(brokerCount);
                    for (int k = 0; k < brokerCount; k++) {
                        JMQ2Broker broker = brokers.get(k);
                        // 1字节长度字符串
                        Serializer.write(broker.getName(), out);
                        // 1字节长度字符串
                        Serializer.write(broker.getAlias(), out);
                        // 1字节数据中心
                        out.writeByte(broker.getDataCenter());
                        // 1字节权限
                        out.writeByte(broker.getPermission().ordinal());
                    }
                }
            }
            // 兼容原来的协议，权重追加到最后
            for (int i = 0; i < count; i++) {
                BrokerCluster cluster = clusters.get(i);
                List<BrokerGroup> groups = cluster.getGroups();
                // 1字节分组数量
                int groupCount = groups == null ? 0 : groups.size();
                for (int j = 0; j < groupCount; j++) {
                    BrokerGroup group = groups.get(j);
                    // 权限
                    out.writeShort(group.getWeight());
                }
            }
            Serializer.write(payload.getAllTopicConfigStrings(), out, 4);
        }
    }

    @Override
    public Object decode(JMQ2Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_CLUSTER_ACK.getCode();
    }
}