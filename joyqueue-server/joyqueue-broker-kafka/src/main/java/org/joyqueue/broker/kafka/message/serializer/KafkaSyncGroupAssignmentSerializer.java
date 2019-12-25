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
package org.joyqueue.broker.kafka.message.serializer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.command.SyncGroupAssignment;
import org.joyqueue.network.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * KafkaSyncGroupAssignmentSerializer
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class KafkaSyncGroupAssignmentSerializer {

    private static final short HEADER_VERSION = 0;

    public static SyncGroupAssignment readAssignment(ByteBuf buffer) throws Exception {
        int length = buffer.readInt();
        short headerVersion = buffer.readShort();
        Map<String, List<Integer>> topicPartitions = Maps.newHashMap();

        int topicSize = buffer.readInt();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = buffer.readInt();
            List<Integer> partitions = (partitionSize == -1 ? Collections.emptyList() : Lists.newArrayListWithCapacity(partitionSize));
            for (int j = 0; j < partitionSize; j++) {
                partitions.add(buffer.readInt());
            }
            topicPartitions.put(topic, partitions);
        }

        int userDataLength = buffer.readInt();
        byte[] userData = null;
        if (userDataLength > 0) {
            userData = new byte[userDataLength];
            buffer.readBytes(userData);
        }

        SyncGroupAssignment syncGroupAssignment = new SyncGroupAssignment();
        syncGroupAssignment.setUserData(userData);
        syncGroupAssignment.setTopicPartitions(topicPartitions);
        return syncGroupAssignment;
    }

    public static void writeAssignment(ByteBuf buffer, SyncGroupAssignment assignment) throws Exception {
        int writerIndex = buffer.writerIndex();
        buffer.writeInt(0);
        buffer.writeShort(HEADER_VERSION);
        buffer.writeInt(assignment.getTopicPartitions().keySet().size());

        for (Map.Entry<String, List<Integer>> entry : assignment.getTopicPartitions().entrySet()) {
            String topic = entry.getKey();
            List<Integer> partitions = entry.getValue();
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);

            if (CollectionUtils.isEmpty(partitions)) {
                // 有些客户端支持不好
                buffer.writeInt(-1);
//                buffer.writeInt(0);
            } else {
                buffer.writeInt(partitions.size());
                for (Integer partition : partitions) {
                    buffer.writeInt(partition);
                }
            }
        }

        if (ArrayUtils.isEmpty(assignment.getUserData())) {
            // 0.9.0客户端问题，不支持-1
            // 有些客户端支持不好
            buffer.writeInt(0);
        } else {
            buffer.writeInt(assignment.getUserData().length);
            buffer.writeBytes(assignment.getUserData());
        }

        buffer.setInt(writerIndex, buffer.writerIndex() - writerIndex - 4);
    }
}