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
 * FetchPartitionMessageResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchPartitionMessageResponse extends JoyQueuePayload {

    private Table<String, Short, FetchPartitionMessageAckData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PARTITION_MESSAGE_RESPONSE.getCode();
    }

    public Table<String, Short, FetchPartitionMessageAckData> getData() {
        return data;
    }

    public void setData(Table<String, Short, FetchPartitionMessageAckData> data) {
        this.data = data;
    }
}