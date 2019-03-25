package com.jd.journalq.store.index;

import com.jd.journalq.store.message.MessageParser;

import java.nio.ByteBuffer;

/**
 * 索引记录
 */
public class IndexItem {

    public final static int STORAGE_SIZE = 8 + 4;

    /**
     * 分区
     */
    private short partition;
    /**
     * 索引，消息在分区内的全局序号
     */
    private long index;
    /**
     * 消息起始位置全局偏移量
     */
    private long offset;
    /**
     * 消息长度
     */
    private int length;

    /**
     * 是否批消息
     */
    private boolean isBatchMessage = false;
    /**
     * 批消息大小
     */
    private short batchMessageSize = 1;

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public IndexItem() {}
    public IndexItem(short partition, long index , int length, long offset) {
        this.partition = partition;
        this.index = index;
        this.length = length;
        this.offset = offset;
    }

    public static IndexItem parseMessage(ByteBuffer byteBuffer, long offset) throws BuildIndexFailedException {
        try {
            return new IndexItem(MessageParser.getShort(byteBuffer,MessageParser.PARTITION),
                    MessageParser.getLong(byteBuffer,MessageParser.INDEX),
                    MessageParser.getInt(byteBuffer,MessageParser.LENGTH),
                    offset);
        }catch (IndexOutOfBoundsException e) {
            throw new BuildIndexFailedException(e);
        }
    }

    public void serializeTo(ByteBuffer buffer){
        buffer.putLong(offset);
        buffer.putInt(length);
    }

    public static IndexItem from(ByteBuffer byteBuffer,short partition, long index){
        IndexItem indexItem = from(byteBuffer);
        indexItem.setPartition(partition);
        indexItem.setIndex(index);
        return indexItem;
    }

    public static IndexItem from(ByteBuffer byteBuffer) {
        IndexItem indexItem = new IndexItem();
        indexItem.setOffset(byteBuffer.getLong());
        indexItem.setLength(byteBuffer.getInt());
        return indexItem;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IndexItem) {
            IndexItem indexItem = (IndexItem) obj;
            return indexItem.getOffset() == getOffset()
                    && indexItem.getIndex() == getIndex()
                    && indexItem.getLength() == indexItem.getLength()
                    && indexItem.getPartition() == getPartition();
        }
        return super.equals(obj);
    }

    public boolean isBatchMessage() {
        return isBatchMessage;
    }

    public void setBatchMessage(boolean batchMessage) {
        isBatchMessage = batchMessage;
    }

    public short getBatchMessageSize() {
        return batchMessageSize;
    }

    public void setBatchMessageSize(short batchMessageSize) {
        this.batchMessageSize = batchMessageSize;
    }
}
