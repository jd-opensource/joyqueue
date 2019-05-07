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

import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * ProduceMessageCommitAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageCommitAck extends JMQPayload {

    private JournalqCode code;

    public ProduceMessageCommitAck() {

    }

    public ProduceMessageCommitAck(JournalqCode code) {
        this.code = code;
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_COMMIT_ACK.getCode();
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }

    public JournalqCode getCode() {
        return code;
    }
}