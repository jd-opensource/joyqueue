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

import java.util.Collections;
import java.util.List;

/**
 * FetchProduceFeedbackAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class FetchProduceFeedbackAck extends JMQPayload {

    private List<FetchProduceFeedbackAckData> data;
    private JournalqCode code;

    public FetchProduceFeedbackAck() {

    }

    public FetchProduceFeedbackAck(JournalqCode code) {
        this.data = Collections.emptyList();
        this.code = code;
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_PRODUCE_FEEDBACK_ACK.getCode();
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }

    public JournalqCode getCode() {
        return code;
    }

    public void setData(List<FetchProduceFeedbackAckData> data) {
        this.data = data;
    }

    public List<FetchProduceFeedbackAckData> getData() {
        return data;
    }
}