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

import org.joyqueue.broker.kafka.command.SyncGroupAssignment;
import org.joyqueue.broker.kafka.coordinator.group.callback.JoinCallback;
import org.joyqueue.broker.kafka.coordinator.group.callback.SyncCallback;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GroupMemberMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class GroupMemberMetadata extends org.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata {

    private SyncGroupAssignment assignment;
    private JoinCallback awaitingJoinCallback;
    private SyncCallback awaitingSyncCallback;
    private boolean isLeaving = false;

    private String clientId;
    private int rebalanceTimeoutMs;
    private Map<String, byte[]> supportedProtocols;

    public GroupMemberMetadata(String memberId, String groupId, String clientId, String clientHost,
                               int rebalanceTimeoutMs, int sessionTimeoutMs, Map<String, byte[]> supportedProtocols) {
        setId(memberId);
        setGroupId(groupId);
        setConnectionHost(clientHost);
        setSessionTimeout(sessionTimeoutMs);
        this.clientId = clientId;
        this.rebalanceTimeoutMs = (rebalanceTimeoutMs <= 0 ? sessionTimeoutMs : rebalanceTimeoutMs);
        this.supportedProtocols = supportedProtocols;
    }

    public int getRebalanceTimeoutMs() {
        return rebalanceTimeoutMs;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isLeaving() {
        return isLeaving;
    }

    public void setLeaving(boolean isLeaving) {
        this.isLeaving = isLeaving;
    }

    public Map<String, byte[]> getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(Map<String, byte[]> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    public SyncGroupAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(SyncGroupAssignment assignment) {
        this.assignment = assignment;
    }

    public JoinCallback getAwaitingJoinCallback() {
        return awaitingJoinCallback;
    }

    public void setAwaitingJoinCallback(JoinCallback awaitingJoinCallback) {
        this.awaitingJoinCallback = awaitingJoinCallback;
    }

    public void setAwaitingSyncCallback(SyncCallback awaitingSyncCallback) {
        this.awaitingSyncCallback = awaitingSyncCallback;
    }

    public SyncCallback getAwaitingSyncCallback() {
        return awaitingSyncCallback;
    }

    /**
     * Check if the provided protocol metadata matches the currently stored metadata.
     */
    public boolean matches(Map<String, byte[]> protocols) {
        if (protocols != null && supportedProtocols != null) {
            if (protocols.size() != this.supportedProtocols.size()) {
                return false;
            }
            Iterator<Map.Entry<String, byte[]>> protocolsIter = protocols.entrySet().iterator();
            while(protocolsIter.hasNext()){
                Map.Entry<String, byte[]> protocolsEntry = (Map.Entry<String, byte[]>) protocolsIter.next();
                byte[] protocolsValue = protocolsEntry.getValue() == null ? null : protocolsEntry.getValue();
                byte[] supportedProtocolsvalue = supportedProtocols.get(protocolsEntry.getKey()) == null ? null : supportedProtocols.get(protocolsEntry.getKey());

                if (!Arrays.equals(protocolsValue, supportedProtocolsvalue)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<String> protocols() {
        if (supportedProtocols != null && !supportedProtocols.isEmpty()) {
            return supportedProtocols.keySet();
        } else {
            return null;
        }
    }

    /**
     * Get metadata corresponding to the provided protocol.
     */
    public byte[] metadata(String protocol) {
        if (supportedProtocols != null && !supportedProtocols.isEmpty()) {
            Set<String> protocols = supportedProtocols.keySet();
            for (String supportProtocol : protocols) {
                if (supportProtocol.equals(protocol)) {
                    return supportedProtocols.get(supportProtocol);
                }
            }
        }
        throw new IllegalArgumentException("Member does not support protocol");
    }

    /**
     * Vote for one of the potential group protocols. This takes into account the protocol preference as
     * indicated by the order of supported protocols and returns the first one also contained in the set
     */
    public String vote(List<String> candidates) {
        if (CollectionUtils.isNotEmpty(candidates)) {
            if (MapUtils.isNotEmpty(supportedProtocols)) {
                for (Map.Entry<String, byte[]> entry : supportedProtocols.entrySet()) {
                    if (candidates.contains(entry.getKey())) {
                        return entry.getKey();
                    }
                }
            }
        }
        throw new IllegalArgumentException("Member does not support any of the candidate protocols");
    }

    @Override
    public String toString() {
        return String.format("[%s,%s,%s,%s,%d]", getId(), getGroupId(), clientId, getConnectionHost(), getSessionTimeout());
    }

}
