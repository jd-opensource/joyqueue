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
package com.jd.joyqueue.network.transport.command;

import com.jd.joyqueue.network.transport.codec.JournalqHeader;

/**
 * JournalqCommand
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class JournalqCommand extends Command {

    public JournalqCommand(JournalqPayload payload) {
        this(payload.type(), payload);
    }

    public JournalqCommand(int type, JournalqPayload payload) {
        setHeader(new JournalqHeader(type));
        setPayload(payload);
    }

    @Override
    public String toString() {
        return header.toString();
    }
}