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
package org.joyqueue.broker.kafka.coordinator.group.domain;

/**
 * Created by zhangkepeng on 17-2-13.
 */
public enum GroupState {
    /**
     * Group has no more members, but lingers until all offsets have expired. This state
     * also represents groups which use Kafka only for offset commits and have no members.
     *
     * action: respond normally to join group from new members
     *         respond to sync group with UNKNOWN_MEMBER_ID
     *         respond to heartbeat with UNKNOWN_MEMBER_ID
     *         respond to leave group with UNKNOWN_MEMBER_ID
     *         respond to offset commit with UNKNOWN_MEMBER_ID
     *         allow offset fetch requests
     * transition: last offsets removed in periodic expiration task => Dead
     *             join group from a new member => PreparingRebalance
     *             group is removed by partition emigration => Dead
     *             group is removed by expiration => Dead
     */
    EMPTY((byte)0),

    /**
     * Group is preparing to rebalance
     *
     * action: respond to heartbeats with REBALANCE_IN_PROGRESS
     *         respond to sync group with REBALANCE_IN_PROGRESS
     *         remove member on leave group request
     *         park join group requests from new or existing members until all expected members have joined
     *         allow offset commits from previous generation
     *         allow offset fetch requests
     * transition: some members have joined by the timeout => AwaitingSync
     *             all members have left the group => Dead
     */
    PREPARINGREBALANCE((byte)1),
    /**
     * Group is awaiting state assignment from the leader
     *
     * action: respond to heartbeats with REBALANCE_IN_PROGRESS
     *         respond to offset commits with REBALANCE_IN_PROGRESS
     *         park sync group requests from followers until transition to Stable
     *         allow offset fetch requests
     * transition: sync group with state assignment received from leader => Stable
     *             join group from new member or existing member with updated metadata => PreparingRebalance
     *             leave group from existing member => PreparingRebalance
     *             member failure detected => PreparingRebalance
     */
    AWAITINGSYNC((byte)2),
    /**
     * Group is stable
     *
     * action: respond to member heartbeats normally
     *         respond to sync group from any member with current assignment
     *         respond to join group from followers with matching metadata with current group metadata
     *         allow offset commits from member of current generation
     *         allow offset fetch requests
     * transition: member failure detected via heartbeat => PreparingRebalance
     *             leave group from existing member => PreparingRebalance
     *             leader join-group received => PreparingRebalance
     *             follower join-group with new metadata => PreparingRebalance
     */
    STABLE((byte)3),
    /**
     * Group has no more members
     *
     * action: respond to join group with UNKNOWN_MEMBER_ID
     *         respond to sync group with UNKNOWN_MEMBER_ID
     *         respond to heartbeat with UNKNOWN_MEMBER_ID
     *         respond to leave group with UNKNOWN_MEMBER_ID
     *         respond to offset commit with UNKNOWN_MEMBER_ID
     *         allow offset fetch requests
     * transition: Dead is a final state before group metadata is cleaned up, so there are no transitions
     */
    DEAD((byte)4);

    private byte state;

    public byte getState() {
        return state;
    }

    GroupState(byte state) {
        this.state = state;
    }
}
