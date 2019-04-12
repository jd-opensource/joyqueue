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
package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.PartitionOffsetsResponse;

/**
 * ListOffsetsHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ListOffsetsResponse extends KafkaRequestOrResponse {

    private Table<String, Integer, PartitionOffsetsResponse> offsetsResponseTable;

    public ListOffsetsResponse() {

    }

    public ListOffsetsResponse(Table<String, Integer, PartitionOffsetsResponse> offsetsResponseTable) {
        this.offsetsResponseTable = offsetsResponseTable;
    }

    public void setOffsetsResponseTable(Table<String, Integer, PartitionOffsetsResponse> offsetsResponseTable) {
        this.offsetsResponseTable = offsetsResponseTable;
    }

    public Table<String, Integer, PartitionOffsetsResponse> getOffsetsResponseTable() {
        return offsetsResponseTable;
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: " + this.getClass().getSimpleName());
        return builder.toString();
    }
}
