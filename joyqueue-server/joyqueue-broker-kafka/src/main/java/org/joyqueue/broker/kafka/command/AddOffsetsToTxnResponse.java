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
package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

/**
 * AddOffsetsToTxnResponse
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class AddOffsetsToTxnResponse extends KafkaRequestOrResponse {

    private short code;

    public AddOffsetsToTxnResponse() {

    }

    public AddOffsetsToTxnResponse(short code) {
        this.code = code;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_OFFSETS_TO_TXN.getCode();
    }
}
