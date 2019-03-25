package com.jd.journalq.nsr.ignite.model;


import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

/**
 * @author lixiaobin6
 * @date 上午11:18 2018/8/6
 */
public class IgniteMessage implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_MESSAGE_ID = "id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CONTENT = "content";
    public static final Integer DEFAULT_ID = 1;

    private String type;
    private String content;

    public IgniteMessage() {
    }

    public IgniteMessage(String type, String content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public Integer getId() {
        return DEFAULT_ID;
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeInt(COLUMN_MESSAGE_ID, DEFAULT_ID);
        writer.writeString(COLUMN_TYPE, type);
        writer.writeString(COLUMN_CONTENT, content);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        reader.readInt(COLUMN_MESSAGE_ID);
        type = reader.readString(COLUMN_TYPE);
        content = reader.readString(COLUMN_CONTENT);
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
