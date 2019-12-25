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

import org.joyqueue.broker.kafka.coordinator.group.domain.GroupDescribe;
import org.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;

/**
 * Created by zhuduohui on 2018/5/17.
 */
public class DescribeGroupsResponse extends KafkaRequestOrResponse {

    private List<GroupDescribe> groups;

    public DescribeGroupsResponse() {

    }

    public DescribeGroupsResponse(List<GroupDescribe> groups) {
        this.groups = groups;
    }

    public List<GroupDescribe> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDescribe> groups) {
        this.groups = groups;
    }

    @Override
    public int type() {
        return KafkaCommandType.DESCRIBE_GROUP.getCode();
    }
}
