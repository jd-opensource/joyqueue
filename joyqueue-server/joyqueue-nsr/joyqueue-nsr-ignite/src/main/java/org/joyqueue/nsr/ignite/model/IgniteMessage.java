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
