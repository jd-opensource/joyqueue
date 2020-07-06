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
package org.joyqueue.sync;

import org.joyqueue.model.domain.Identity;

import java.util.List;

/**
 * 应用信息
 */
public class ApplicationInfo {
    //应用
    private long id;
    //代码
    private String code;
    //名称
    private String name;
    //系统
    private String system;
    //部门
    private String department;
    //来源
    private int source;
    //所有者
    private UserInfo owner;
    //成员
    private List<UserInfo> members;
    //当前操作用户
    private Identity user;
    //别名或英文名
    private String aliasCode;
    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public UserInfo getOwner() {
        return owner;
    }

    public void setOwner(UserInfo owner) {
        this.owner = owner;
    }

    public List<UserInfo> getMembers() {
        return members;
    }

    public void setMembers(List<UserInfo> members) {
        this.members = members;
    }

    public Identity getUser() {
        return user;
    }

    public void setUser(Identity user) {
        this.user = user;
    }

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAliasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationInfo info = (ApplicationInfo) o;

        if (source != info.source) {
            return false;
        }
        if (code != null ? !code.equals(info.code) : info.code != null) {
            return false;
        }
        if (name != null ? !name.equals(info.name) : info.name != null) {
            return false;
        }
        if (system != null ? !system.equals(info.system) : info.system != null) {
            return false;
        }
        if (department != null ? !department.equals(info.department) : info.department != null) {
            return false;
        }
        if (owner != null ? !owner.equals(info.owner) : info.owner != null) {
            return false;
        }
        return members != null ? members.equals(info.members) : info.members == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (system != null ? system.hashCode() : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        result = 31 * result + source;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (members != null ? members.hashCode() : 0);
        return result;
    }
}
