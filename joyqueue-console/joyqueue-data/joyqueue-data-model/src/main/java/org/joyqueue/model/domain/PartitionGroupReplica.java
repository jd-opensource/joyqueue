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
package org.joyqueue.model.domain;

import org.joyqueue.model.domain.nsr.BaseNsrModel;

import java.util.ArrayList;
import java.util.List;

public class PartitionGroupReplica  extends BaseNsrModel implements Comparable<PartitionGroupReplica> {

    public static final int ROLE_DYNAMIC = 0;

    public static final int ROLE_MASTER = 1;

    public static final int ROLE_SLAVE = 2;

    public static final int ROLE_LEARNER = 3;
    //信息不同步
    public static final int STATE_OUT_SYNC = 2;
    private Namespace namespace;
    private Topic topic;
    private int groupNo;
    private int brokerId;
    private int role = ROLE_DYNAMIC;
    private Broker broker;
    protected List<Integer> outSyncReplicas = new ArrayList<>();

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public int getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(int groupNo) {
        this.groupNo = groupNo;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public List<Integer> getOutSyncReplicas() {
        return outSyncReplicas;
    }

    public void setOutSyncReplicas(List<Integer> outSyncReplicas) {
        this.outSyncReplicas = outSyncReplicas;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public Broker getBroker() {
        return broker;
    }

    @Override
    public int compareTo(PartitionGroupReplica o) {
        return Long.compare(brokerId, o.getBrokerId());
    }
}
