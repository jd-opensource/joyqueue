package com.jd.journalq.nsr.ignite.model;


import com.jd.journalq.domain.DataCenter;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;


/**
 * @author lixiaobin6
 * @date 上午11:18 2018/8/6
 */
public class IgniteDataCenter extends DataCenter implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_REGION = "region";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_URL = "url";

    @Override
    public String getId() {
        return getRegion() + "_" + getCode();
    }


    public IgniteDataCenter(DataCenter dataCenter) {
        setRegion(dataCenter.getRegion());
        setCode(dataCenter.getCode());
        setUrl(dataCenter.getUrl());
        setName(dataCenter.getName());
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_REGION, getRegion());
        writer.writeString(COLUMN_CODE, getCode());
        writer.writeString(COLUMN_NAME, getName());
        writer.writeString(COLUMN_URL, getUrl());
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        this.region = reader.readString(COLUMN_REGION);
        this.code = reader.readString(COLUMN_CODE);
        this.name = reader.readString(COLUMN_NAME);
        this.setUrl(reader.readString(COLUMN_URL));
    }
}
