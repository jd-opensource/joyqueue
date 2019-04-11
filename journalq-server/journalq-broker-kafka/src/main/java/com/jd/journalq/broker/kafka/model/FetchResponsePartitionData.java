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
package com.jd.journalq.broker.kafka.model;

import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.KafkaErrorCode;

import java.util.List;

/**
 * Created by zhangkepeng on 16-8-17.
 *
 */
public class FetchResponsePartitionData {

    private short error = KafkaErrorCode.NONE;
    private long hw = -1L;
    private List<KafkaBrokerMessage> messages;
    private int bytes;

    public FetchResponsePartitionData() {

    }

    public FetchResponsePartitionData(short error) {
        this.error = error;
    }

    public FetchResponsePartitionData(short error, long hw, List<KafkaBrokerMessage> messages) {
        this.error = error;
        this.hw = hw;
        this.messages = messages;
    }

    public short getError() {
        return error;
    }

    public void setError(short error) {
        this.error = error;
    }

    public long getHw() {
        return hw;
    }

    public void setHw(long hw) {
        this.hw = hw;
    }

    public void setMessages(List<KafkaBrokerMessage> messages) {
        this.messages = messages;
    }

    public List<KafkaBrokerMessage> getMessages() {
        return messages;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public int getBytes() {
        return bytes;
    }
}

