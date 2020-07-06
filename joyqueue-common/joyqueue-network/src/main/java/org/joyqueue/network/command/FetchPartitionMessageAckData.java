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

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.message.BrokerMessage;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * FetchPartitionData
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchPartitionMessageAckData {

    private List<BrokerMessage> messages;
    private List<ByteBuffer> buffers;
    private JoyQueueCode code;

    public FetchPartitionMessageAckData() {

    }

    public FetchPartitionMessageAckData(JoyQueueCode code) {
        this.code = code;
        this.buffers = Collections.emptyList();
    }

    public FetchPartitionMessageAckData(List<BrokerMessage> messages, JoyQueueCode code) {
        this.messages = messages;
        this.code = code;
    }

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public List<ByteBuffer> getBuffers() {
        return buffers;
    }

    public void setBuffers(List<ByteBuffer> buffers) {
        this.buffers = buffers;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public int getSize() {
        if (buffers == null) {
            return 0;
        }
        return buffers.size();
    }

    public int getTraffic() {
        if (buffers == null) {
            return 0;
        }
        int result = 0;
        for (ByteBuffer buffer : buffers) {
            result += buffer.limit();
        }
        return result;
    }
}