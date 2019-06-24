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
package com.jd.joyqueue.client.internal.producer.domain;

import com.jd.joyqueue.exception.JournalqCode;

import java.util.List;

/**
 * SendBatchResultData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class SendBatchResultData {

    private List<SendResult> result;
    private JournalqCode code;

    public List<SendResult> getResult() {
        return result;
    }

    public void setResult(List<SendResult> result) {
        this.result = result;
    }

    public JournalqCode getCode() {
        return code;
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }
}