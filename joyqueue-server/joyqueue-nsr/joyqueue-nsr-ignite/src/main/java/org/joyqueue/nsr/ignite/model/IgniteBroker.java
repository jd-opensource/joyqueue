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


import org.joyqueue.domain.Broker;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

/**
 * @author lixiaobin6
 * @date 上午11:18 2018/8/6
 */
public class IgniteBroker extends Broker implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_BROKER_ID = "id";
    public static final String COLUMN_IP = "ip";
    public static final String COLUMN_PORT = "port";
    public static final String COLUMN_DATA_CENTER = "data_center";
    public static final String COLUMN_RETRY_TYPE = "retry_type";
    public static final String COLUMN_PERMISSION = "permission";

    public IgniteBroker(Broker broker) {
        this.ip = broker.getIp();
        this.id = broker.getId();
        this.port = broker.getPort();
        this.dataCenter = broker.getDataCenter();
        this.retryType = broker.getRetryType();
        this.permission = broker.getPermission();
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeInt(COLUMN_BROKER_ID, id);
        writer.writeString(COLUMN_IP, ip);
        writer.writeInt(COLUMN_PORT, port);
        writer.writeString(COLUMN_DATA_CENTER, dataCenter);
        writer.writeString(COLUMN_RETRY_TYPE, retryType);
        writer.writeString(COLUMN_PERMISSION,permission.getName());
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        this.id = reader.readInt(COLUMN_BROKER_ID);
        this.ip = reader.readString(COLUMN_IP);
        this.port = reader.readInt(COLUMN_PORT);
        this.dataCenter = reader.readString(COLUMN_DATA_CENTER);
        this.retryType = reader.readString(COLUMN_RETRY_TYPE);
        this.permission = PermissionEnum.value(reader.readString(COLUMN_PERMISSION));
    }
}
