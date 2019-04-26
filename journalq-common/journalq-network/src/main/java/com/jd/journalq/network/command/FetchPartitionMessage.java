/**
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
package com.jd.journalq.network.command;

import com.google.common.collect.Table;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * FetchPartitionMessage
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchPartitionMessage extends JMQPayload {

    public static final long NONE_INDEX = -1;

    private Table<String, Short, FetchPartitionMessageData> partitions;
    private String app;

    @Override
    public int type() {
        return JournalqCommandType.FETCH_PARTITION_MESSAGE.getCode();
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