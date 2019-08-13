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
package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.message.BrokerMessage;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * FetchTopicMessageAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchTopicMessageAckData {

    private List<BrokerMessage> messages;
    private List<ByteBuffer> buffers;
    private JoyQueueCode code;

    public FetchTopicMessageAckData() {

    }

    public FetchTopicMessageAckData(JoyQueueCode code) {
        this.code = code;
        this.buffers = Collections.emptyList();
    }

    public FetchTopicMessageAckData(List<BrokerMessage> messages, JoyQueueCode code) {
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
        int size = 0;
        for (ByteBuffer buffer : buffers) {
            size += buffer.limit();
        }
        return size;
    }
}