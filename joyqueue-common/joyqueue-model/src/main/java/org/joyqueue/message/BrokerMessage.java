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
package org.joyqueue.message;

/**
 *
 属性 | 长度(Byte) | 说明
 -- | -- |--
 存储长度 | 4 |
 分区 | 2 | PartitionId
 消息序号 | 8 | Partition Index消息在分区内的全局唯一序号
 魔法标示 | 2 |
 系统字段 | 1 |
 优先级 | 1 |
 客户端地址 | 8 |
 发送时间 | 8 |
 存储时间 | 4 | 保存与发送时间的偏移量
 消息体CRC | 8 |
 消息体长度 | 4 |
 消息体 | 变长 |
 业务ID长度 | 1 |
 业务ID | 变长 |
 属性长度| 2 |
 属性 | 变长 |
 扩展字段长度| 4 |
 扩展字段 | 变长 |
 *
 *
 * @author lining11
 * Date: 2018/8/17
 */
public class BrokerMessage extends Message implements JoyQueueLog {
    public static final short MAGIC_CODE = 0x1234;
    public static final short MAGIC_LOG_CODE = 0x3456;//只是做个引用方便

    private long msgIndexNo;
    private byte[] clientIp;
    private int storeTime;
    private long startTime;
    private byte source = SourceType.JOYQUEUE.getValue();
    private int size;
    private byte type = TYPE_MESSAGE;
    private int term;
    private byte[] extension;
    private boolean batch = false;

    public byte getSource() {
        return source;
    }

    public void setSource(byte source) {
        this.source = source;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getMsgIndexNo() {
        return msgIndexNo;
    }

    public void setMsgIndexNo(long msgIndexNo) {
        this.msgIndexNo = msgIndexNo;
    }

    public byte[] getClientIp() {
        return clientIp;
    }

    public void setClientIp(byte[] clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public int getStoreTime() {
        return storeTime;
    }

    @Override
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setStoreTime(int storeTime) {
        this.storeTime = storeTime;
    }

    @Override
    public int getSize() {

        if (size <= 0){
            return super.getSize();
        }
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getTerm() {
        return term;
    }

    public byte[] getExtension() {
        return extension;
    }

    public void setExtension(byte[] extension) {
        this.extension = extension;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public boolean isBatch() {
        return batch;
    }

    public BrokerMessage clone() {
        BrokerMessage cloneMessage = new BrokerMessage();
        cloneMessage.setSource(source);
        cloneMessage.setStoreTime(storeTime);
        cloneMessage.setPartition(partition);
        cloneMessage.setClientIp(clientIp);
        cloneMessage.setMsgIndexNo(msgIndexNo);
        cloneMessage.setSize(size);
        cloneMessage.setStartTime(startTime);
        cloneMessage.setTerm(term);
        cloneMessage.setApp(app);
        cloneMessage.setAttributes(attributes);
        cloneMessage.setBusinessId(businessId);
        cloneMessage.setCompressed(compressed);
        cloneMessage.setTopic(topic);
        cloneMessage.setTxId(txId);
        cloneMessage.setFlag(flag);
        cloneMessage.setType(type);
        cloneMessage.setBody(body);
        cloneMessage.setBodyCRC(bodyCRC);
        cloneMessage.setExtension(extension);
        cloneMessage.setBatch(batch);
        return cloneMessage;
    }
}