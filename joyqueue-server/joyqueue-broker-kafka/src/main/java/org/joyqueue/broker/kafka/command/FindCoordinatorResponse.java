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
import org.joyqueue.broker.kafka.model.KafkaBroker;

/**
 * Created by zhangkepeng on 17-2-9.
 */
public class FindCoordinatorResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private KafkaBroker broker;

    public FindCoordinatorResponse(short errorCode, KafkaBroker broker) {
        this.errorCode = errorCode;
        this.broker = broker;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public KafkaBroker getBroker() {
        return broker;
    }

    public void setBroker(KafkaBroker broker) {
        this.broker = broker;
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return responseStringBuilder.toString();
    }
}
