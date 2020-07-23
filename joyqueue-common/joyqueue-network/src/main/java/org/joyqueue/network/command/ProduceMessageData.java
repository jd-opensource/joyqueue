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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.message.BrokerMessage;

import java.util.List;

/**
 * ProduceMessageData
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageData {

    private String txId;
    private int timeout;
    private QosLevel qosLevel;
    private List<BrokerMessage> messages;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setQosLevel(QosLevel qosLevel) {
        this.qosLevel = qosLevel;
    }

    public QosLevel getQosLevel() {
        return qosLevel;
    }

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public int getSize() {
        if (messages == null) {
            return 0;
        }
        return messages.size();
    }

    public int getTraffic() {
        if (messages == null) {
            return 0;
        }
        int result = 0;
        for (BrokerMessage message : messages) {
            result += ObjectUtils.defaultIfNull(message.getByteBody(), ArrayUtils.EMPTY_BYTE_ARRAY).length;
        }
        return result;
    }
}