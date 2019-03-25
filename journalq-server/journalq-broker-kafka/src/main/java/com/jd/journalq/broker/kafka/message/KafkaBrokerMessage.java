package com.jd.journalq.broker.kafka.message;

import java.util.Map;

/**
 * KafkaBrokerMessage
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/9
 */
public class KafkaBrokerMessage {

    public static final byte INVALID_MAGIC = -1;

    private int size;
    private byte magic = INVALID_MAGIC;
    private int crc;
    private short attribute;
    private long timestamp;
    private byte[] key;
    private byte[] value;
    private long offset;
    private int compressionCodecType;
    private boolean batch;
    private short flag;
    private Map<byte[], byte[]> header;

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public byte getMagic() {
        return magic;
    }

    public void setMagic(byte magic) {
        this.magic = magic;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public short getAttribute() {
        return attribute;
    }

    public void setAttribute(short attribute) {
        this.attribute = attribute;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setCompressionCodecType(int compressionCodecType) {
        this.compressionCodecType = compressionCodecType;
    }

    public int getCompressionCodecType() {
        return compressionCodecType;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public boolean isBatch() {
        return batch;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public short getFlag() {
        return flag;
    }

    public void setHeader(Map<byte[], byte[]> header) {
        this.header = header;
    }

    public Map<byte[], byte[]> getHeader() {
        return header;
    }
}