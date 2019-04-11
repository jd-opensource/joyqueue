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

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * ProduceMessagePrepareAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessagePrepareAck extends JMQPayload {

    private String txId;
    private JMQCode code;

    public ProduceMessagePrepareAck() {

    }

    public ProduceMessagePrepareAck(JMQCode code) {
        this.code = code;
    }

    public ProduceMessagePrepareAck(String txId, JMQCode code) {
        this.txId = txId;
        this.code = code;
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE_ACK.getCode();
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }
}