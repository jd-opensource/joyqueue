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
import java.util.Map;

/**
 * Created by zhangkepeng on 17-2-9.
 */
public class JoinGroupResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private int generationId;
    private String groupProtocol;
    private String memberId;
    private String leaderId;
    private Map<String, ByteBuffer> members;

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public int getGenerationId() {
        return generationId;
    }

    public void setGenerationId(int generationId) {
        this.generationId = generationId;
    }

    public String getGroupProtocol() {
        return groupProtocol;
    }

    public void setGroupProtocol(String groupProtocol) {
        this.groupProtocol = groupProtocol;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public Map<String, ByteBuffer> getMembers() {
        return members;
    }

    public void setMembers(Map<String, ByteBuffer> members) {
        this.members = members;
    }

    @Override
    public int type() {
        return KafkaCommandType.JOIN_GROUP.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return responseStringBuilder.toString();
    }
}