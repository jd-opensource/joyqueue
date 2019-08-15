package io.chubao.joyqueue.model;

/**
 *  操作人
 *  Created by chenyanying3 on 2018-11-12.
 */
public class QOperator implements Query {
    protected int role;
    protected Long userId;
    protected String userCode;
    protected String userName;
    protected Boolean admin;

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}
