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
package org.joyqueue.nsr.ignite.model;

import org.joyqueue.domain.Config;
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
