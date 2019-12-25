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

import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class IgnitePartitionGroupReplica extends Replica implements IgniteBaseModel, Binarylizable {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_BROKER_ID = "broker_id";
    public static final String COLUMN_GROUP_NO = "group_no";

    //private String id;

    public IgnitePartitionGroupReplica(Replica replica) {
        this( replica.getTopic(), replica.getBrokerId(), replica.getGroup());
    }

    public IgnitePartitionGroupReplica(TopicName topic, Integer brokerId, Integer groupNo) {
        this.topic = topic;
        this.brokerId = brokerId;
        this.group = groupNo;
    }


    @Override
    public String getId() {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(group).append(SPLICE).append(brokerId).toString();
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_NAMESPACE, topic.getNamespace());
        writer.writeString(COLUMN_TOPIC, topic.getCode());
        writer.writeInt(COLUMN_BROKER_ID, brokerId);
        writer.writeInt(COLUMN_GROUP_NO, group);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        //this.id = reader.readString(COLUMN_ID);
        this.topic = new TopicName(reader.readString(COLUMN_TOPIC), reader.readString(COLUMN_NAMESPACE));
        this.brokerId = reader.readInt(COLUMN_BROKER_ID);
        this.group = reader.readInt(COLUMN_GROUP_NO);
    }
}
