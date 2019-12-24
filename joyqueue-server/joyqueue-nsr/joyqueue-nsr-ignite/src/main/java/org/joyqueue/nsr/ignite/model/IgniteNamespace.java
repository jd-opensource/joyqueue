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

import org.joyqueue.domain.Namespace;
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
