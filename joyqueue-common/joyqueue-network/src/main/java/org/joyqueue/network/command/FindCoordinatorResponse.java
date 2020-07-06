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

import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * FindCoordinatorResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class FindCoordinatorResponse extends JoyQueuePayload {

    private Map<String, FindCoordinatorAckData> coordinators;

    @Override
    public int type() {
        return JoyQueueCommandType.FIND_COORDINATOR_RESPONSE.getCode();
    }

    public void setCoordinators(Map<String, FindCoordinatorAckData> coordinators) {
        this.coordinators = coordinators;
    }

    public Map<String, FindCoordinatorAckData> getCoordinators() {
        return coordinators;
    }
}