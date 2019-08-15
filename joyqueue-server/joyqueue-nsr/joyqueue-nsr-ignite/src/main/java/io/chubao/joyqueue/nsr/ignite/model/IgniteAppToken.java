package io.chubao.joyqueue.nsr.ignite.model;

import io.chubao.joyqueue.domain.AppToken;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

import java.util.Date;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class IgniteAppToken extends AppToken implements IgniteBaseModel, Binarylizable {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_APP = "app";
    public static final String COLUMN_TOKEN = "token";
    public static final String COLUMN_EFFECTIVE_TIME = "effective_time";
    public static final String COLUMN_EXPIRATION_TIME = "expiration_time";

    public IgniteAppToken(AppToken token) {
        this(token.getId(), token.getApp(), token.getToken(),token.getEffectiveTime(), token.getExpirationTime());
    }

    public IgniteAppToken(Long id, String app, String token, Date effectiveTime, Date expirationTime) {
        this.id = id;
        this.app = app;
        this.token = token;
        this.effectiveTime = effectiveTime;
        this.expirationTime = expirationTime;
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeLong(COLUMN_ID, id);
        writer.writeString(COLUMN_APP, app);
        writer.writeString(COLUMN_TOKEN, token);
        writer.writeDate(COLUMN_EFFECTIVE_TIME, effectiveTime);
        writer.writeDate(COLUMN_EXPIRATION_TIME, expirationTime);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        this.id = reader.readLong(COLUMN_ID);
        this.app = reader.readString(COLUMN_APP);
        this.token = reader.readString(COLUMN_TOKEN);
        this.effectiveTime = reader.readDate(COLUMN_EFFECTIVE_TIME);
        this.expirationTime = reader.readDate(COLUMN_EXPIRATION_TIME);
    }
}
