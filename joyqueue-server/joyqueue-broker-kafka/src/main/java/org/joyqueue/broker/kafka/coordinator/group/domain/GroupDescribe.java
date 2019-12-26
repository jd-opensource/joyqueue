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


import java.util.List;

/**
 * Created by zhuduohui on 2018/5/17.
 */
public class GroupDescribe {

    private short errCode;
    private String groupId;
    private String state;
    private String protocolType;
    private String protocol;
    private List<GroupMemberMetadata> members;

    public short getErrCode() {
        return errCode;
    }

    public void setErrCode(short errCode) {
        this.errCode = errCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<GroupMemberMetadata> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberMetadata> members) {
        this.members = members;
    }
}

