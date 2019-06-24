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
package com.jd.joyqueue.network.command;

import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.network.transport.command.JournalqPayload;

/**
 * ProduceMessagePrepareResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessagePrepareResponse extends JournalqPayload {

    private String txId;
    private JournalqCode code;

    public ProduceMessagePrepareResponse() {

    }

    public ProduceMessagePrepareResponse(JournalqCode code) {
        this.code = code;
    }

    public ProduceMessagePrepareResponse(String txId, JournalqCode code) {
        this.txId = txId;
        this.code = code;
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_PREPARE_RESPONSE.getCode();
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }

    public JournalqCode getCode() {
        return code;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }
}