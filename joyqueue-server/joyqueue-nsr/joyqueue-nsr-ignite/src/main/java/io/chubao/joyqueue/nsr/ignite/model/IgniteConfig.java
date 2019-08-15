package io.chubao.joyqueue.nsr.ignite.model;

import io.chubao.joyqueue.domain.Config;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class IgniteConfig extends Config implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CFG_GROUP = "cfg_group";
    public static final String COLUMN_CFG_KEY = "cfg_key";
    public static final String COLUMN_CFG_VALUE = "cfg_value";

    //private String id;

    public IgniteConfig(Config config) {
        super(config.getGroup(), config.getKey(), config.getValue());
    }

    @Override
    public String getId() {
        return new StringBuilder(30).append(group).append(SPLICE).append(key).toString();
    }



    public static String getId(String group, String key) {
        return new StringBuilder(30).append(group).append(SPLICE).append(key).toString();
    }


    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_CFG_GROUP, group);
        writer.writeString(COLUMN_CFG_KEY, key);
        writer.writeString(COLUMN_CFG_VALUE, value);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        //this.id = reader.readString(COLUMN_ID);
        this.group = reader.readString(COLUMN_CFG_GROUP);
        this.key = reader.readString(COLUMN_CFG_KEY);
        this.value = reader.readString(COLUMN_CFG_VALUE);
    }
}
