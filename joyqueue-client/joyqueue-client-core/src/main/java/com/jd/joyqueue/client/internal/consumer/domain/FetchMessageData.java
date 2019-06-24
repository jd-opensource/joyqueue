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
package com.jd.joyqueue.client.internal.consumer.domain;

import com.jd.joyqueue.exception.JournalqCode;

import java.util.Collections;
import java.util.List;

/**
 * FetchMessageData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchMessageData {

    private List<ConsumeMessage> messages;
    private JournalqCode code;

    public FetchMessageData() {
    }

    public FetchMessageData(JournalqCode code) {
        this.messages = Collections.emptyList();
        this.code = code;
    }

    public FetchMessageData(List<ConsumeMessage> messages, JournalqCode code) {
        this.messages = messages;
        this.code = code;
    }

    public List<ConsumeMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConsumeMessage> messages) {
        this.messages = messages;
    }

    public JournalqCode getCode() {
        return code;
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }
}