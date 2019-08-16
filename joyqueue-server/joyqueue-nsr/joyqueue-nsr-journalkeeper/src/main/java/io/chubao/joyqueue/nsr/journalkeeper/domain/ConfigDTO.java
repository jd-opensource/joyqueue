package io.chubao.joyqueue.nsr.journalkeeper.domain;

/**
 * ConfigDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConfigDTO extends BaseDTO {

    private String id;
    private String key;
    private String value;
    private String group;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}