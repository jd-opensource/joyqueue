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

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * ProduceMessageCommitResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class ProduceMessageCommitResponse extends JoyQueuePayload {

    private JoyQueueCode code;

    public ProduceMessageCommitResponse() {

    }

    public ProduceMessageCommitResponse(JoyQueueCode code) {
        this.code = code;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_COMMIT_RESPONSE.getCode();
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}