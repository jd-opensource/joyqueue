package io.chubao.joyqueue.model.domain;


import io.chubao.joyqueue.model.domain.nsr.BaseNsrModel;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public class Config extends BaseNsrModel {
    public static final String GROUP_DATACENTER = "datacenter";

    protected String name;
    protected String group;
    protected String key;
    protected String value;
    protected Integer password;
    protected Identity owner;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getPassword() {
        return password;
    }

    public void setPassword(Integer password) {
        this.password = password;
    }

    @Override
    public Application clone() {
        try {
            return (Application) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
