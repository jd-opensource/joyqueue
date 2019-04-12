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
import com.jd.journalq.broker.kafka.model.FetchResponsePartitionData;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class FetchResponse extends KafkaRequestOrResponse {

    private Table<String, Integer, FetchResponsePartitionData> fetchResponses;

    public Table<String, Integer, FetchResponsePartitionData> getFetchResponses() {
        return fetchResponses;
    }

    public void setFetchResponses(Table<String, Integer, FetchResponsePartitionData> fetchResponses) {
        this.fetchResponses = fetchResponses;
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        responseStringBuilder.append("fetchResponses: " + fetchResponses);
        return responseStringBuilder.toString();
    }
}