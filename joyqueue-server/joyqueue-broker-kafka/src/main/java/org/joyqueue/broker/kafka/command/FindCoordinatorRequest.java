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

import org.joyqueue.broker.kafka.coordinator.CoordinatorType;
import org.joyqueue.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 17-2-9.
 */
public class FindCoordinatorRequest extends KafkaRequestOrResponse {

    private String coordinatorKey;
    private CoordinatorType coordinatorType;

    public String getCoordinatorKey() {
        return coordinatorKey;
    }

    public void setCoordinatorKey(String coordinatorKey) {
        this.coordinatorKey = coordinatorKey;
    }

    public CoordinatorType getCoordinatorType() {
        return coordinatorType;
    }

    public void setCoordinatorType(CoordinatorType coordinatorType) {
        this.coordinatorType = coordinatorType;
    }

    @Override
    public String toString() {
        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("Name: " + this.getClass().getSimpleName());
        requestStringBuilder.append("; coordinatorKey: " + coordinatorKey);
        return requestStringBuilder.toString();
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }
}
