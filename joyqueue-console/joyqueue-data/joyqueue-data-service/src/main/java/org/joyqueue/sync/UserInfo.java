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

import org.joyqueue.model.domain.Identifier;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.domain.User.UserRole;
import org.joyqueue.model.domain.User.UserStatus;

/**
 * 用户信息
 */
public class UserInfo implements Identifier {
    //ID
    private long id;
    //代码
    private String code;
    //姓名
    private String name;
    //组织ID
    private String orgId;
    //组织名称
    private String orgName;
    //邮件
    private String email;
    //移动电话
    private String mobile;
    //状态
    private int status = UserStatus.ENABLE.value();
    //签名
    private int sign;
    //当前库中的角色
    private int role = UserRole.NORMAL.value();
    //当前操作用户
    private Identity user;

    public UserInfo() {
    }

    public UserInfo(String code) {
        this.code = code;
    }

    public UserInfo(String code, String name) {
        this.code = code;
        this.name = name;
    }

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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public Identity getUser() {
        return user;
    }

    public void setUser(Identity user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        int result = orgId != null ? orgId.hashCode() : 0;
        result = 31 * result + (orgName != null ? orgName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (mobile != null ? mobile.hashCode() : 0);
        result = 31 * result + status;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * 赋值信息
     *
     * @param user
     */
    public void apply(final User user) {
        if (user == null) {
            return;
        }
        setId(user.getId());
        setCode(user.getCode());
        setName(user.getName());
        setEmail(user.getEmail());
        setMobile(user.getMobile());
        setOrgId(user.getOrgId());
        setOrgName(user.getOrgName());
        setStatus(user.getStatus());
        setSign(user.getSign());
        setRole(user.getRole());
    }

    /**
     * 赋值信息
     *
     * @param info
     */
    public void apply(final UserInfo info) {
        if (info == null) {
            return;
        }
        setId(info.getId());
        setCode(info.getCode());
        setName(info.getName());
        setEmail(info.getEmail());
        setMobile(info.getMobile());
        setOrgId(info.getOrgId());
        setOrgName(info.getOrgName());
        setStatus(info.getStatus());
        setSign(info.getSign());
        setRole(info.getRole());
    }

    /**
     * 转换成用户对象
     *
     * @return
     */
    public User toUser() {
        return toUser(hashCode());
    }

    /**
     * 转换成用户对象
     *
     * @param sign
     * @return
     */
    public User toUser(int sign) {
        User result = new User();
        toUser(result, sign);
        return result;
    }

    /**
     * 转换成用户对象
     *
     * @param sign
     * @return
     */
    public void toUser(final User target, final int sign) {
        if (target.getId() <= 0) {
            target.setId(id);
        }
        target.setCode(code);
        target.setName(name);
        target.setEmail(email);
        target.setMobile(mobile);
        target.setOrgId(orgId);
        target.setOrgName(orgName);
        target.setStatus(status);
        target.setSign(sign);
        target.setRole(role);
        if (user != null) {
            target.setUpdateBy(user);
        }
        if (target.getCreateBy() == null) {
            target.setCreateBy(user);
        }
    }

}
