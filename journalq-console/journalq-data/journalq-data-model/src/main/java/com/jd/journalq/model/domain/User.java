package com.jd.journalq.model.domain;

import java.util.Date;

/**
 * Created by yangyang36 on 2018/9/12.
 */
public class User extends BaseModel implements Identifier {

    private String code;
    private String name;
    private String orgId;
    private String orgName;
    private String email;
    private String mobile;
    //签名
    private int sign;
    private int role;
    private int status;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public enum UserRole {

        NORMAL(0, "普通用户"),
        ADMIN(1, "管理员");

        private int value;
        private String description;

        UserRole(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return value;
        }

        public String description() {
            return description;
        }

        public static UserRole valueOf(int value) {
            switch (value) {
                case 0:
                    return NORMAL;
                case 1:
                    return ADMIN;
                default:
                    return NORMAL;
            }
        }
    }

    public enum UserStatus {

        ENABLE(1, "可用"),
        UNABLE(0, "禁用");

        private int value;
        private String description;

        UserStatus(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return value;
        }

        public String description() {
            return description;
        }

        public static UserStatus valueOf(int value) {
            switch (value) {
                case 1:
                    return ENABLE;
                case 0:
                    return UNABLE;
                default:
                    return ENABLE;
            }
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", orgId='" + orgId + '\'' +
                ", orgName='" + orgName + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", sign=" + sign +
                ", role=" + role +
                ", status=" + status +
                ", createBy=" + createBy +
                ", createTime=" + createTime +
                ", updateBy=" + updateBy +
                ", updateTime=" + updateTime +
                '}';
    }
}
