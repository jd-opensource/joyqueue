package com.jd.journalq.nsr.ignite.model;

import com.jd.journalq.domain.Namespace;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

public class IgniteNamespace extends Namespace implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_NAME = "name";

    @Override
    public String getId() {
        return code;
    }

    public IgniteNamespace(Namespace namespace) {
        this.code = namespace.getCode();
        this.name = namespace.getName();
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_CODE, code);
        writer.writeString(COLUMN_NAME, name);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        this.code = reader.readString(COLUMN_CODE);
        this.name = reader.readString(COLUMN_NAME);
    }
}
