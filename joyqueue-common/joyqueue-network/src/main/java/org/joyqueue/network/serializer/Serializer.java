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
package org.joyqueue.network.serializer;

import com.google.common.base.Charsets;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.domain.TopicType;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.BrokerPrepare;
import org.joyqueue.message.BrokerRollback;
import org.joyqueue.message.Message;
import org.joyqueue.toolkit.io.Compressors;
import org.joyqueue.toolkit.io.Zip;
import org.joyqueue.toolkit.io.ZipUtil;
import org.joyqueue.toolkit.retry.RetryPolicy;
import org.joyqueue.toolkit.serialize.AbstractSerializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.JoyQueueHeader;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 负责broker端消息的序列化
 *
 * @author lining11
 * Date: 2018/8/17
 */
public class Serializer extends AbstractSerializer {

    public static final byte BYTE_SIZE = 1;
    public static final byte SHORT_SIZE = 2;
    public static final byte INT_SIZE = 4;

    private static final int fixBodyLength =
            4 // size
                    + 2 // partition
                    + 8 // index
                    + 4 // term
                    + 2 // magic code
                    + 2 // sys code
                    + 2 // flag
                    + 1 // priority
                    + 16 // client ip
                    + 8 // send time
                    + 4 // store time
                    + 8 // crc
                    + 4 // body length
                    + 1 // app
                    + 1 // biz id length
                    + 2 // prop length
                    + 4; // expand length

    private static final byte MESSAGE_VERSION_V0 = 0;
    private static final byte MESSAGE_VERSION_V1 = 1;
    private static final byte CURRENT_MESSAGE_VERSION = MESSAGE_VERSION_V1;

    /**
     * 写入存储消息
     *
     * @param message 存储消息
     * @param out     输出缓冲区
     * @throws Exception 序列化异常
     */
    public static void writeBrokerMessage(BrokerMessage message, ByteBuf out) throws Exception {
        int size = sizeOf(message);
        writeBrokerMessage(message, out, size);
    }

    /**
     * 写入存储消息
     *
     * @param messages 存储消息
     * @param out      输出缓冲区
     * @throws Exception 发生异常时抛出异常
     */
    public static void writeBrokerMessage(final BrokerMessage[] messages, final ByteBuf out) throws Exception {
        if (out == null) {
            return;
        }

        int count = messages == null ? 0 : messages.length;
        out.writeShort(count);

        for (int i = 0; i < count; i++) {
            writeBrokerMessage(messages[i], out);
        }
    }

    public static int sizeOf(BrokerMessage message) {
        int bodyLength = fixBodyLength;

        // body length
        ByteBuffer buffer = message.getBody();
        int length = buffer == null ? 0 : buffer.remaining();
        bodyLength += length;


        byte[] bytes;

        bytes = getBytes(message.getApp(), Charsets.UTF_8);
        bodyLength += bytes == null? 0: bytes.length;

        bytes = getBytes(message.getBusinessId(), Charsets.UTF_8);
        bodyLength += bytes == null? 0: bytes.length;

        bytes = getBytes(toProperties(message.getAttributes()), Charsets.UTF_8);
        bodyLength += bytes == null? 0: bytes.length;

        bytes = message.getExtension();
        bodyLength += bytes == null? 0: bytes.length;


        return bodyLength;
    }

    /**
     * 写入存储消息
     *
     * @param message 存储消息
     * @param out     输出缓冲区
     * @param size size
     * @throws Exception 序列化异常
     */
    public static void writeBrokerMessage(BrokerMessage message, ByteBuf out, int size) throws Exception {
        // 记录写入的起始位置
        int begin = out.writerIndex();
        // 4个字节的消息长度需要计算出来
        out.writeInt(size);
        // 分区
        out.writeShort(message.getPartition());
        //消息序号
        out.writeLong(message.getMsgIndexNo());
        // 任期
        out.writeInt(message.getTerm());
        // 2个字节的魔法标识
        out.writeShort(BrokerMessage.MAGIC_CODE);

        //   | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 |
        //TODO   1-1：压缩标识 2-2：顺序消息 3-4: 消息来源，包括Jmq，kafka，mqtt 5-7：压缩算法, 8: 地址类型, ipv4, ipv6, 9-12：消息版本号, 13-13: 批消息标识, 14-16: 其他,预留未用
        short sysCode = (byte) (message.isCompressed() ? 1 : 0);
        sysCode |= ((message.isOrdered() ? 1 : 0) << 1) & 0x3;

        sysCode |= ((message.getSource() << 2) & (3 << 2));
        // compressor
        if (message.isCompressed()) {
            sysCode |= ((message.getCompressionType().getType() << 4) & (7 << 4));
        }
        if (message.getClientIp().length < 7) {
            sysCode |= (1 << 7);
        }

        sysCode |= ((CURRENT_MESSAGE_VERSION << 8) & (15 << 8));
        sysCode |= ((message.isBatch() ? 1 : 0) << 12);
        out.writeShort(sysCode);

        // 1字节优先级
        out.writeByte(message.getPriority());
        // 2字节业务标签
        // 16字节的客户端地址
        byte [] clientIp = new byte[16];
        if(message.getClientIp() != null) {
            System.arraycopy(message.getClientIp(), 0, clientIp,0, Math.min(message.getClientIp().length, clientIp.length));
        }
        out.writeBytes(clientIp);
//        out.put(message.getClientIp() == null ? new byte[16] : message.getClientIp());
//        if (message.getClientIp().length < 7){
//            out.put(new byte[10]);
//        }
        // 8字节发送时间
        out.writeLong(message.getStartTime());
        // 4字节存储时间（相对发送时间的偏移）
        out.writeInt(0);
        // 8字节消息体CRC
        out.writeLong(message.getBodyCRC());
        // 2字节业务标签
        out.writeShort(message.getFlag());

        // 4字节消息体大小
        // 消息体（字节数组）
        if (message.getByteBody() != null) {
            write(message.getBody(), out, true);
        } else {
            out.writeInt(0);
        }

        // 1字节业务ID长度
        // 业务ID（字节数组）
        write(message.getBusinessId(), out);
        // 2字节属性长度
        // 属性（字节数组）
        write(toProperties(message.getAttributes()), out, 2);
        // 4字节扩展字段大小
        // 扩展字段（字节数组）
        write(message.getExtension(), out, true);
        // 1字节应用长度
        // 应用（字节数组）
        write(message.getApp(), out);
        // 重写总长度
//        int end = out.position();
//        size = end - begin;
        message.setSize(size);
//        out.position(begin);
//        out.putInt(size);
//        out.position(end);
    }

    public static void write(final byte[] value, final ByteBuf out, final boolean writeLength) {
        int length = value == null ? 0 : value.length;
        if (writeLength) {
            out.writeInt(length);
        }
        if (length > 0) {
            out.writeBytes(value);
        }
    }

    public static void write(final byte[] value, final ByteBuffer out, final int lengthSize) {
        write(value, out, true, lengthSize);
    }

    public static void write(final byte[] value, final ByteBuffer out, final boolean writeLength, final int lengthSize) {
        int length = value == null ? 0 : value.length;
        if (writeLength) {
            if (lengthSize == 1) {
                out.put((byte) length);
            } else if (lengthSize == 2) {
                out.putShort((short) length);
            } else {
                out.putInt(length);
            }
        }
        if (length > 0) {
            out.put(value);
        }
    }

    public static BrokerMessage readBrokerMessage(final ByteBuf in) throws Exception {
        BrokerMessage message = new BrokerMessage();

        // 4个字节的消息长度
        int totalLength = in.readInt();

        message.setPartition(in.readShort());
        message.setMsgIndexNo(in.readLong());
        message.setTerm(in.readInt());
        in.readShort();

        // 2字节系统字段
        short sysCode = in.readShort();
        byte version = (byte) ((sysCode >> 8) & 15);
        boolean isIpv4 = (sysCode & (1 << 7)) > 1;

        message.setCompressed(((sysCode & 0x1) > 0));
        message.setOrdered(((sysCode & 0x2) > 0));
        message.setSource((byte) (sysCode >> 2 & 0x3));
        message.setBatch(((sysCode >> 12) == 1));

        if (version == MESSAGE_VERSION_V0) {
            message.setCompressionType(Message.CompressionType.valueOf((sysCode >> 4 & 3)));
            isIpv4 = true;
        } else {
            message.setCompressionType(Message.CompressionType.valueOf((sysCode >> 4 & 7)));
        }

        message.setPriority(in.readByte());
        if (isIpv4) {
            message.setClientIp(readBytes(in, 6));
            readBytes(in, 10);
        } else {
            message.setClientIp(readBytes(in, 16));
        }


        message.setStartTime(in.readLong());
        message.setStoreTime(in.readInt());
        message.setBodyCRC(in.readLong());
        message.setFlag(in.readShort());

        int bodyLength = in.readInt();
        message.setBody(readBytes(in, bodyLength));
        message.setBusinessId(readString(in));
        message.setAttributes(toStringMap(readString(in, 2)));

        int extensionLength = in.readInt();
        message.setExtension(readBytes(in, extensionLength));
        message.setApp(readString(in));

        return message;
    }

    // size+MAGIC+type
    public static ByteBuf write(BrokerPrepare prepare, ByteBuf out) throws Exception {

        int begin = out.writerIndex();
        //长度占位
        out.writeInt(0);
        //魔术字符
        out.writeShort(BrokerMessage.MAGIC_LOG_CODE);
        //命令类型
        out.writeByte(prepare.getType());
        //事务开启时间
        out.writeLong(prepare.getStartTime());
        //存储时间
        out.writeLong(prepare.getStoreTime());
        //主题
        write(prepare.getTopic(), out);
        //事物ID
        write(prepare.getTxId(), out, SHORT_SIZE);
        //查询标识
        write(prepare.getQueryId(), out, SHORT_SIZE);
        //扩展属性
        write(prepare.getAttrs(), out);
        // 重写总长度
        int end = out.writerIndex();
        int size = end - begin;
        prepare.setSize(size);
        out.writerIndex(begin);
        out.writeInt(size);
        out.writerIndex(end);
        return out;
    }

    /**
     * @param rollback 序列化命令
     * @param out 输出buf
     * @return 返回值
     * @throws Exception 序列化/反序列化错误
     */
    public static ByteBuf write(BrokerRollback rollback, ByteBuf out) throws Exception {

        int begin = out.writerIndex();
        //长度占位
        out.writeInt(0);
        //魔术字符
        out.writeShort(BrokerMessage.MAGIC_LOG_CODE);
        //命令类型
        out.writeByte(rollback.getType());
        //接收时间
        out.writeLong(rollback.getStartTime());
        //存储时间
        out.writeLong(rollback.getStoreTime());
        //主题
        write(rollback.getTopic(), out);
        //事物ID
        write(rollback.getTxId(), out, SHORT_SIZE);
        write(rollback.getAttrs(), out);
        // 重写总长度
        int end = out.writerIndex();
        int size = end - begin;
        rollback.setSize(size);
        out.writerIndex(begin);
        out.writeInt(size);
        out.writerIndex(end);
        return out;
    }


    public static void write(final TopicConfig topicConfig, final ByteBuf out, int version) throws Exception {
        // 2 1 string
        write(topicConfig.getName().getFullName(), out);
        // 3 short
        out.writeShort(topicConfig.getPartitions());
        // 4 byte
        out.writeByte(topicConfig.getType().code());
        Set<Short> priorityPartitions = topicConfig.getPriorityPartitions();
        if (null == priorityPartitions) {
            out.writeInt(0);
        } else {
            // 6 int
            out.writeInt(priorityPartitions.size());
            // 7 short array
            for (Short partition : priorityPartitions) {
                out.writeShort(partition);
            }
        }
        if (null == topicConfig.getPartitionGroups()) {
            out.writeInt(0);
        } else {
            // 8 int
            out.writeInt(topicConfig.getPartitionGroups().size());
            // 9 array
            for (PartitionGroup group : topicConfig.getPartitionGroups().values()) {
                write(group, out, version);
            }
        }
    }
    public static TopicConfig readTopicConfig(final ByteBuf in, final int version) throws Exception {
        TopicConfig topicConfig = new TopicConfig();
        // 2 1 string
        topicConfig.setName(TopicName.parse(Serializer.readString(in)));
        // 3 short
        topicConfig.setPartitions(in.readShort());
        // 4 byte
        topicConfig.setType(TopicConfig.Type.valueOf(in.readByte()));
        int priorityPartitionSize = in.readInt();
        Set<Short> priorityPartitions = new TreeSet();
        for (int i = 0; i < priorityPartitionSize; i++) {
            priorityPartitions.add(in.readShort());
        }
        // 8 int
        int partitionGroupSize = in.readInt();
        Map<Integer,PartitionGroup> partitionGroups = new HashMap<>(partitionGroupSize);
        for(int i =0;i<partitionGroupSize;i++){
            PartitionGroup group = readPartitionGroup(in, version);
            partitionGroups.put(group.getGroup(),group);
        }
        topicConfig.setPartitionGroups(partitionGroups);
        return topicConfig;
    }
    public static void write(final Topic topic, final ByteBuf out) throws Exception {
        // 2 1 string
        write(topic.getName().getFullName(), out);
        // 3 short
        out.writeShort(topic.getPartitions());
        // 4 byte
        out.writeByte(topic.getType().code());
        Set<Short> priorityPartitions = topic.getPriorityPartitions();
        if (null == priorityPartitions) {
            out.writeInt(0);
        } else {
            // 5 byte
            // 6 int
            out.writeInt(priorityPartitions.size());
            // 7 short array
            for (Short partition : priorityPartitions) {
                out.writeShort(partition);
            }
        }
    }
    public static Topic readTopic(final ByteBuf in) throws Exception {
        Topic topic = new Topic();
        // 2 1 string
        topic.setName(TopicName.parse(Serializer.readString(in)));
        // 3 short
        topic.setPartitions(in.readShort());
        // 4 byte
        topic.setType(TopicConfig.Type.valueOf(in.readByte()));
        // 5 boolean
        int priorityPartitionSize = in.readInt();
        Set<Short> priorityPartitions = new TreeSet();
        for (int i = 0; i < priorityPartitionSize; i++) {
            priorityPartitions.add(in.readShort());
        }
        return topic;
    }
    public static void write(final AppToken appToken, final ByteBuf out) throws Exception {
        out.writeLong(appToken.getId());
        write(appToken.getApp(),out);
        write(appToken.getToken(),out);
        out.writeLong(appToken.getEffectiveTime().getTime());
        out.writeLong(appToken.getExpirationTime().getTime());
    }
    public static AppToken readAppToken(final ByteBuf in) throws Exception {
        AppToken appToken = new AppToken();
        appToken.setId(in.readLong());
        appToken.setApp(readString(in));
        appToken.setToken(readString(in));
        appToken.setEffectiveTime(new Date(in.readLong()));
        appToken.setExpirationTime(new Date(in.readLong()));
        return appToken;
    }

    public static void write(final PartitionGroup partitionGroup, final ByteBuf out, int version) throws Exception {
        Serializer.write(partitionGroup.getTopic().getFullName(), out);
        out.writeInt(partitionGroup.getLeader());
        if(null == partitionGroup.getIsrs()){
            out.writeInt(0);
        }else {
            out.writeInt(partitionGroup.getIsrs().size());
            for (Integer isr : partitionGroup.getIsrs()) {
                out.writeInt(isr);
            }
        }
        if(null==partitionGroup.getLearners()){
            out.writeInt(0);
        }else {
            out.writeInt(partitionGroup.getLearners().size());
            for (Integer learner : partitionGroup.getLearners()) {
                out.writeInt(learner);
            }
        }
        out.writeInt(partitionGroup.getTerm());
        out.writeInt(partitionGroup.getGroup());
        if(null==partitionGroup.getPartitions()){
            out.writeInt(0);
        }else {
            out.writeInt(partitionGroup.getPartitions().size());
            for (Short partition : partitionGroup.getPartitions()) {
                out.writeShort(partition);
            }
        }
        if(null==partitionGroup.getReplicas()){
            out.writeInt(0);
        }else {
            out.writeInt(partitionGroup.getReplicas().size());
            for (Integer replica : partitionGroup.getReplicas()) {
                out.writeInt(replica);
            }
        }
        out.writeInt(partitionGroup.getElectType().type());
        Map<Integer,Broker> brokers = partitionGroup.getBrokers();
        if(null==brokers){
            out.writeInt(0);
        }else{
            out.writeInt(brokers.size());
            for(Broker broker : brokers.values()){
                Serializer.write(broker,out);
            }
        }

        if (version >= JoyQueueHeader.VERSION_V3) {
            out.writeInt(partitionGroup.getRecLeader());
        }
    }

    public static PartitionGroup readPartitionGroup(final ByteBuf in, final int version) throws Exception {
        PartitionGroup group = new PartitionGroup();
        group.setTopic(TopicName.parse(Serializer.readString(in)));
        group.setLeader(in.readInt());
        Set<Integer> isrs = new TreeSet();
        int istLen = in.readInt();
        for(int i =0;i<istLen;i++){
            isrs.add(in.readInt());
        }
        group.setIsrs(isrs);
        int learnerLen = in.readInt();
        Set<Integer> learners = new TreeSet();
        for(int i =0 ;i<learnerLen;i++){
            learners.add(in.readInt());
        }
        group.setTerm(in.readInt());
        group.setGroup(in.readInt());
        Set<Short> partitions = new TreeSet();
        int partitionsLen = in.readInt();
        for(int i =0;i<partitionsLen;i++){
            partitions.add(in.readShort());
        }
        group.setPartitions(partitions);
        Set<Integer> replicaGroups = new TreeSet();
        int replicaGroupsLen = in.readInt();
        for(int i =0;i<replicaGroupsLen;i++){
            replicaGroups.add(in.readInt());
        }
        group.setReplicas(replicaGroups);
        group.setElectType(PartitionGroup.ElectType.valueOf(in.readInt()));
        int brokerSize = in.readInt();
        Map<Integer,Broker> brokers = new HashMap<Integer, Broker>(brokerSize);
        for(int i = 0;i<brokerSize;i++){
            Broker broker = readBroker(in);
            brokers.put(broker.getId(),broker);
        }
        group.setBrokers(brokers);

        if (version >= JoyQueueHeader.VERSION_V3) {
            group.setRecLeader(in.readInt());
        }

        return group;
    }
    public static void write(final Broker broker, final ByteBuf out) throws Exception {
        // 2.int
        out.writeInt(broker.getId());
        // 3.String(1)
        write(broker.getIp(),out);
        // 4.int
        out.writeInt(broker.getPort());
        // 5.String(1)
        write(broker.getDataCenter(),out);
        // 6.String(1)
        write(broker.getRetryType(),out);
    }
    public static Broker readBroker(final ByteBuf in) throws Exception {
        Broker broker = new Broker();
        // 2.int
        broker.setId(in.readInt());
        // 3.String(1)
        broker.setIp(readString(in));
        // 4.int
        broker.setPort(in.readInt());
        // 5.String(1)
        broker.setDataCenter(readString(in));
        // 6.String(1)
        broker.setRetryType(readString(in));
        return broker;
    }

    public static void write(int version, final Producer producer, final ByteBuf out) throws Exception {
        write(producer.getApp(),out);
        write(producer.getTopic().getFullName(),out);
        out.writeByte(producer.getType().getValue());
        out.writeByte(producer.getClientType().value());
        Producer.ProducerPolicy policy = producer.getProducerPolicy();
        boolean hasPolicy = (null != policy);
        out.writeBoolean(hasPolicy);
        if(hasPolicy){
            out.writeInt(policy.getTimeOut());
            out.writeBoolean(policy.getArchive());
            out.writeBoolean(policy.getNearby());
            out.writeBoolean(policy.isSingle());
            Set<String> blackList = policy.getBlackList();
            if(null!=blackList&&blackList.size()>0){
                out.writeBoolean(true);
                String blackListStr = Arrays.toString(blackList.toArray());
                write(blackListStr.substring(1,blackListStr.length()-1),out,Serializer.SHORT_SIZE);
            }else{
                out.writeBoolean(false);
            }
            Map<String,Short> wight = policy.getWeight();
            if(null!=wight&&wight.size()>0){
                out.writeBoolean(true);
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Short> entry : wight.entrySet()) {
                    stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                }
                write(stringBuilder.substring(0, stringBuilder.length() - 1),out);
            }else{
                out.writeBoolean(false);
            }
        }

        Producer.ProducerLimitPolicy limitPolicy = producer.getLimitPolicy();
        if (limitPolicy == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeInt(limitPolicy.getTps());
            out.writeInt(limitPolicy.getTraffic());
        }
    }
    public static Producer readProducer(int version, final ByteBuf in) throws Exception {
        Producer producer = new Producer();
        producer.setApp(readString(in));
        producer.setTopic(TopicName.parse(readString(in)));
        producer.setType(Subscription.Type.valueOf(in.readByte()));
        producer.setClientType(ClientType.valueOf(in.readByte()));
        boolean hasPolicy = in.readBoolean();
        if(hasPolicy){
            Producer.ProducerPolicy.Builder policy = Producer.ProducerPolicy.Builder.build();
            policy.timeout(in.readInt()).archive(in.readBoolean()).nearby(in.readBoolean()).single(in.readBoolean());
            boolean hasBlackList = in.readBoolean();
            if(hasBlackList){
                policy.blackList(readString(in,Serializer.SHORT_SIZE));
            }
            boolean hasWight = in.readBoolean();
            if(hasWight){
                policy.weight(readString(in));
            }
            producer.setProducerPolicy(policy.create());
        }

        boolean hasLimitPolicy = in.readBoolean();
        if (hasLimitPolicy) {
            Producer.ProducerLimitPolicy limitPolicy = new Producer.ProducerLimitPolicy();
            limitPolicy.setTps(in.readInt());
            limitPolicy.setTraffic(in.readInt());
            producer.setLimitPolicy(limitPolicy);
        }

        return producer;
    }

    public static void write(int version, final Consumer consumer, final ByteBuf out) throws Exception {
        write(consumer.getApp(),out);
        write(consumer.getTopic().getFullName(),out);
        out.writeByte(consumer.getType().getValue());
        out.writeByte(consumer.getClientType().value());
        if (version >= JoyQueueHeader.VERSION_V3) {
            out.writeByte(consumer.getTopicType().code());
        }
        Consumer.ConsumerPolicy consumerPolicy = consumer.getConsumerPolicy();
        RetryPolicy retryPolicy = consumer.getRetryPolicy();
        boolean hasConsumerPolicy = (null != consumerPolicy);
        boolean hasRetryPolicy = (null != retryPolicy);
        out.writeBoolean(hasConsumerPolicy);
        if(hasConsumerPolicy){
            out.writeInt(consumerPolicy.getAckTimeout());
            out.writeShort(consumerPolicy.getBatchSize());
            out.writeInt(consumerPolicy.getConcurrent());
            out.writeInt(consumerPolicy.getDelay());
            out.writeInt(consumerPolicy.getErrTimes());
            out.writeInt(consumerPolicy.getMaxPartitionNum());
            out.writeInt(consumerPolicy.getReadRetryProbability());
            out.writeBoolean(consumerPolicy.getArchive());
            Set<String> blackList = consumerPolicy.getBlackList();
            if(null!=blackList&&blackList.size()>0){
                out.writeBoolean(true);
                String blackListStr = Arrays.toString(blackList.toArray());
                write(blackListStr.substring(1,blackListStr.length()-1),out,Serializer.SHORT_SIZE);
            }else{
                out.writeBoolean(false);
            }
            out.writeBoolean(consumerPolicy.getNearby());
            out.writeBoolean(consumerPolicy.getPaused());
            out.writeBoolean(consumerPolicy.getRetry());
            out.writeBoolean(consumerPolicy.getSeq());
        }
        out.writeBoolean(hasRetryPolicy);
        if(hasRetryPolicy){
            if(null!=retryPolicy.getUseExponentialBackOff()){
                out.writeBoolean(retryPolicy.getUseExponentialBackOff());
                out.writeDouble(retryPolicy.getBackOffMultiplier());
            }else{
                out.writeBoolean(false);
                out.writeDouble(0);
            }
            out.writeInt(retryPolicy.getExpireTime());
            out.writeInt(retryPolicy.getMaxRetryDelay());
            out.writeInt(retryPolicy.getMaxRetrys());
            out.writeInt(retryPolicy.getRetryDelay());
        }

        Consumer.ConsumerLimitPolicy limitPolicy = consumer.getLimitPolicy();
        if (limitPolicy == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeInt(limitPolicy.getTps());
            out.writeInt(limitPolicy.getTraffic());
        }
    }
    public static Consumer readConsumer(int version, final ByteBuf in) throws Exception {
        Consumer consumer = new Consumer();
        consumer.setApp(readString(in));
        consumer.setTopic(TopicName.parse(readString(in)));
        consumer.setType(Subscription.Type.valueOf(in.readByte()));
        consumer.setClientType(ClientType.valueOf(in.readByte()));
        if (version >= JoyQueueHeader.VERSION_V3) {
            consumer.setTopicType(TopicType.valueOf(in.readByte()));
        }
        boolean hasConsumerPolicy = in.readBoolean();
        if(hasConsumerPolicy){
            Consumer.ConsumerPolicy.Builder consumerPolicy = Consumer.ConsumerPolicy.Builder.build();
            consumerPolicy.ackTimeout(in.readInt())
                    .batchSize(in.readShort())
                    .concurrent(in.readInt())
                    .delay(in.readInt())
                    .errTimes(in.readInt())
                    .maxPartitionNum(in.readInt())
                    .retryReadProbability(in.readInt())
                    .archive(in.readBoolean());
            boolean hasBlackList = in.readBoolean();
            if(hasBlackList){
                consumerPolicy.blackList(readString(in,Serializer.SHORT_SIZE));
            }
            consumerPolicy.nearby(in.readBoolean())
                    .paused(in.readBoolean())
                    .retry(in.readBoolean())
                    .seq(in.readBoolean());
            consumer.setConsumerPolicy(consumerPolicy.create());
        }
        boolean hasRetryPolicy = in.readBoolean();
        if(hasRetryPolicy){
            RetryPolicy.Builder retryPolicy = RetryPolicy.Builder.build();
            retryPolicy.useExponentialBackOff(in.readBoolean())
                    .backOffMultiplier(in.readDouble())
                    .expireTime(in.readInt())
                    .maxRetryDelay(in.readInt())
                    .maxRetrys(in.readInt())
                    .retryDelay(in.readInt());
            consumer.setRetryPolicy(retryPolicy.create());
        }

        boolean hasLimitPolicy = in.readBoolean();
        if (hasLimitPolicy) {
            Consumer.ConsumerLimitPolicy limitPolicy = new Consumer.ConsumerLimitPolicy();
            limitPolicy.setTps(in.readInt());
            limitPolicy.setTraffic(in.readInt());
            consumer.setLimitPolicy(limitPolicy);
        }

        return consumer;
    }
    /**
     * 写字符串
     *
     * @param value      字符串
     * @param out        输出缓冲区
     * @param lengthSize 长度字节数
     * @throws java.lang.Exception 序列化异常
     */
    public static void write(final String value, final ByteBuf out, final int lengthSize) throws Exception {
        write(value, out, lengthSize, false);
    }

    public static void write(final String value, final ByteBuffer out, final int lengthSize) {
        write(value, out, true, lengthSize);
    }

    public static void write(final String value, final ByteBuffer out, final boolean writeLength, final int lengthSize) {
        if (out == null) {
            return;
        }
        if (value != null && !value.isEmpty()) {
            byte[] bytes = getBytes(value, Charsets.UTF_8);
            write(bytes, out, writeLength, lengthSize);
        } else {
            write((byte[]) null, out, writeLength, lengthSize);
        }
    }

    /**
     * 写字符串(长度&lt;=255)
     *
     * @param value 字符串
     * @param out   输出缓冲区
     * @throws java.lang.Exception 序列化异常
     */
    public static void write(final String value, final ByteBuf out) throws Exception {
        write(value, out, 1, false);
    }

    /**
     * 写字符串
     *
     * @param value      字符串
     * @param out        输出缓冲区
     * @param lengthSize 长度字节数
     * @param compressed 是否进行压缩
     * @throws java.lang.Exception 序列化异常
     */
    public static void write(final String value, final ByteBuf out, final int lengthSize,
                             final boolean compressed) throws Exception {
        if (out == null) {
            return;
        }
        if (value != null && !value.isEmpty()) {
            byte[] bytes = getBytes(value, Charsets.UTF_8);
            if (compressed) {
                bytes = Compressors.compress(bytes, 0, bytes.length, Zip.INSTANCE);
            }
            write(bytes.length, out, lengthSize);
            out.writeBytes(bytes);
        } else {
            write(0, out, lengthSize);
        }
    }

    /**
     * 写整数
     *
     * @param value      整数
     * @param out        输出
     * @param lengthSize 长度字节数
     */
    public static void write(final int value, final ByteBuf out, final int lengthSize) {
        if (out == null) {
            return;
        }
        switch (lengthSize) {
            case BYTE_SIZE:
                out.writeByte(value);
                break;
            case SHORT_SIZE:
                out.writeShort(value);
                break;
            case INT_SIZE:
                out.writeInt(value);
                break;
        }
    }

    /**
     * 写数据
     *
     * @param value 数据源
     * @param out   输出缓冲区
     */
    public static void write(final byte[] value, final ByteBuf out) {
        ByteBuffer wrap = ByteBuffer.wrap(value);
        write(wrap, out, true);
    }

    /**
     * 写数据
     *
     * @param value 数据源
     * @param out   输出缓冲区
     */
    public static void write(final ByteBuffer value, final ByteBuf out) {
        write(value, out, true);
    }

    /**
     * 写数据
     *
     * @param value       数据源
     * @param out         输出缓冲区
     * @param writeLength 是否写长度
     */
    public static void write(final ByteBuffer value, final ByteBuf out, final boolean writeLength) {
        int length = value == null ? 0 : value.remaining();
        if (writeLength) {
            out.writeInt(length);
        }
        if (length > 0) {
            if (value.hasArray()) {
                out.writeBytes(value.array(), value.arrayOffset() + value.position(), value.remaining());
            } else {
                out.writeBytes(value.slice());
            }
        }
    }

    /**
     * 写入map数据
     * @param <K> Key
     * @param <V> Value
     * @param hashMap map对象
     * @param out 序列化流
     * @throws Exception 序列化/反序列化错误
     */
    public static <K, V> void write(final Map<K, V> hashMap, ByteBuf out) throws Exception {
        JoyQueueMapTools.write(hashMap, out);
    }

    /**
     * 读取字符串，字符长度&lt;=255
     *
     * @param in 输入缓冲区
     * @return 返回值 字符串
     * @throws java.lang.Exception 序列化异常
     */
    public static String readString(final ByteBuffer in) throws Exception {
        return readString(in, 1, false);
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @return 返回值 字符串
     * @throws java.lang.Exception 序列化异常
     */
    public static String readString(final ByteBuffer in, final int lengthSize) throws Exception {
        return readString(in, lengthSize, false);
    }

    /**
     * 读取字符串，字符长度&lt;=255
     *
     * @param in 输入缓冲区
     * @return 返回值 字符串
     * @throws java.lang.Exception 序列化异常
     */
    public static String readString(final ByteBuf in) throws Exception {
        return readString(in, 1, false);
    }

    /**
     * 读取字符串，前面有一个字符串长度字节
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @param compressed 压缩标示
     * @return 返回值 字符串
     * @throws java.lang.Exception 序列化异常
     */
    public static String readString(final ByteBuf in, final int lengthSize, final boolean compressed) throws Exception {
        int length;
        if (lengthSize == 1) {
            length = in.readByte();
        } else if (lengthSize == 2) {
            length = in.readShort();
        } else {
            length = in.readInt();
        }
        return read(in, length, compressed, "UTF-8");
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param length     长度
     * @param compressed 压缩
     * @param charset    字符集
     * @return 返回值 字符串
     * @throws Exception 序列化/反序列化错误
     */
    public static String read(final ByteBuf in, final int length, final boolean compressed, String charset) throws
            Exception {
        if (length <= 0) {
            return null;
        }

        byte[] bytes = readBytes(in, length);
        try {
            if (compressed) {
                bytes = ZipUtil.decompressByZlib(bytes, 0, bytes.length);
            }

            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8";
            }
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    /**
     * 读取字节数
     *
     * @param in     输入缓冲区
     * @param length 长度
     */
    private static byte[] readBytes(final ByteBuf in, final int length) {
        if (in == null || length <= 0) {
            return new byte[0];
        }
        int len = in.readableBytes();
        if (len == 0) {
            return new byte[0];
        }
        if (length < len) {
            len = length;
        }

        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        return bytes;
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @return 返回值 字符串
     * @throws java.lang.Exception 序列化异常
     */
    public static String readString(final ByteBuf in, final int lengthSize) throws Exception {
        return readString(in, lengthSize, false);
    }

    /**
     * 读取字符串，前面有一个字符串长度字节
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @param compressed 压缩标示
     * @return 返回值 字符串
     * @throws java.lang.Exception 序列化异常
     */
    public static String readString(final ByteBuffer in, final int lengthSize, final boolean compressed) throws Exception {
        int length;
        if (lengthSize == 1) {
            byte[] bytes = new byte[1];
            in.get(bytes);
            length = bytes[0] & 0xff;
        } else if (lengthSize == 2) {
            length = in.getShort();
        } else {
            length = in.getInt();
        }
        return read(in, length, compressed, "UTF-8");
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param length     长度
     * @param compressed 压缩
     * @param charset    字符集
     * @return 返回值 字符串
     * @throws Exception 序列化/反序列化错误
     */
    public static String read(final ByteBuffer in, final int length, final boolean compressed, String charset) throws Exception {
        if (length <= 0) {
            return null;
        }
        byte[] bytes = readBytes(in, length);
        try {
            if (compressed) {
                bytes = Compressors.decompress(bytes, 0, bytes.length, Zip.INSTANCE);
            }

            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8";
            }
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }
}
