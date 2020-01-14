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

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by zhangkepeng on 17-2-9.
 */
public class JoinGroupRequest extends KafkaRequestOrResponse {

    public static final String UNKNOWN_MEMBER_ID = "";

    private String groupId;
    private int sessionTimeout;
    private int rebalanceTimeout;
    private String memberId;
    private String protocolType;
    private List<ProtocolMetadata> groupProtocols;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getRebalanceTimeout() {
        return rebalanceTimeout;
    }

    public void setRebalanceTimeout(int rebalanceTimeout) {
        this.rebalanceTimeout = rebalanceTimeout;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public List<ProtocolMetadata> getGroupProtocols() {
        return groupProtocols;
    }

    public void setGroupProtocols(List<ProtocolMetadata> groupProtocols) {
        this.groupProtocols = groupProtocols;
    }

    public static class ProtocolMetadata {
        private final String name;
        private final ByteBuffer metadata;

        public ProtocolMetadata(String name, ByteBuffer metadata) {
            this.name = name;
            this.metadata = metadata;
        }

        public String name() {
            return name;
        }

        public ByteBuffer metadata() {
            return metadata;
        }
    }

    @Override
    public String toString() {
        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("Name: " + this.getClass().getSimpleName());
        requestStringBuilder.append("; groupId: " + groupId);
        return requestStringBuilder.toString();
    }

    @Override
    public int type() {
        return KafkaCommandType.JOIN_GROUP.getCode();
    }
}
