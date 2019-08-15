package io.chubao.joyqueue.domain;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class Config {
    protected String group;
    protected String key;
    protected String value;

    public String getId(){
        return new StringBuilder(30).append(group).append(".").append(key).toString();
    }

    public String getGroup() {
        return group;
    }

    public Config() {

    }

    public Config(String group, String key, String value) {
        this.group = group;
        this.key = key;
        this.value = value;
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

    @Override
    public String toString() {
        return "Config{" +
                "group='" + group + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
