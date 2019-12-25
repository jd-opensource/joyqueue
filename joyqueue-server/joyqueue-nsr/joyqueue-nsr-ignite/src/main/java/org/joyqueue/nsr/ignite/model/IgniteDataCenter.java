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


import org.joyqueue.domain.DataCenter;
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
