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
package com.jd.journalq.store;

import com.jd.journalq.exception.JournalqCode;

import java.nio.ByteBuffer;


/**
 * 读消息结果
 */
public class ReadResult {
    /**
     * 状态码
     */
    private JournalqCode code;

    /**
     * 消息数组
     */
    private ByteBuffer[] messages;

    /**
     * 给定index超过队尾，说明暂时没有消息可以消费了。
     */
    private boolean eop;

    public JournalqCode getCode() {
        return code;
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }

    public ByteBuffer[] getMessages() {
        return messages;
    }

    public void setMessages(ByteBuffer[] messages) {
        this.messages = messages;
    }

    public boolean isEop() {
        return eop;
    }

    public void setEop(boolean eop) {
        this.eop = eop;
    }
}
