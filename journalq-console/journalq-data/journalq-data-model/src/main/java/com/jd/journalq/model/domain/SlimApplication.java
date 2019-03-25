package com.jd.journalq.model.domain;

public class SlimApplication {
    private String code;
    private Identity owner;
    private String department;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Identity getOwner() {
        return owner;
    }

    public void setOwner(Identity owner) {
        this.owner = owner;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
