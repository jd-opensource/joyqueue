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
package org.joyqueue.network.command;

import com.google.common.collect.Table;
import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchPartitionMessageRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchPartitionMessageRequest extends JoyQueuePayload {

    public static final long NONE_INDEX = -1;

    private Table<String, Short, FetchPartitionMessageData> partitions;
    private String app;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PARTITION_MESSAGE_REQUEST.getCode();
    }

    public void setPartitions(Table<String, Short, FetchPartitionMessageData> partitions) {
        this.partitions = partitions;
    }

    public Table<String, Short, FetchPartitionMessageData> getPartitions() {
        return partitions;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }
}