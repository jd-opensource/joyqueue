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
import com.jd.journalq.network.domain.BrokerNode;

/**
 * FindCoordinatorAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class FindCoordinatorAckData {

    private BrokerNode node;
    private JournalqCode code;

    public FindCoordinatorAckData() {

    }

    public FindCoordinatorAckData(JournalqCode code) {
        this.code = code;
    }

    public FindCoordinatorAckData(BrokerNode node, JournalqCode code) {
        this.node = node;
        this.code = code;
    }

    public BrokerNode getNode() {
        return node;
    }

    public void setNode(BrokerNode node) {
        this.node = node;
    }

    public JournalqCode getCode() {
        return code;
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }
}