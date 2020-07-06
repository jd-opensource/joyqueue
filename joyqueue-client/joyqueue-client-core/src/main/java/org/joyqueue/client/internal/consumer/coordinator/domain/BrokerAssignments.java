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
package org.joyqueue.client.internal.consumer.coordinator.domain;

import java.util.List;

/**
 * BrokerAssignments
 *
 * author: gaohaoxiang
 * date: 2018/12/6
 */
public class BrokerAssignments {

    private List<BrokerAssignment> assignments;

    public BrokerAssignments() {

    }

    public BrokerAssignments(List<BrokerAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<BrokerAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<BrokerAssignment> assignments) {
        this.assignments = assignments;
    }

    @Override
    public String toString() {
        return "BrokerAssignments{" +
                "assignments=" + assignments +
                '}';
    }
}