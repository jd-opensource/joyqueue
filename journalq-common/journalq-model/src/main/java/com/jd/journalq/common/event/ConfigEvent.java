package com.jd.journalq.common.event;

public class ConfigEvent extends MetaEvent {
    private String group;
    private String key;
    private String value;

    public ConfigEvent() {
    }

    public ConfigEvent(EventType type, String group, String key, String value) {
        super(type);
        this.group = group;
        this.key = key;
        this.value = value;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGroup() {
        return group;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static ConfigEvent add(String group, String key, String value) {
        return new ConfigEvent(EventType.ADD_CONFIG, group, key, value);
    }

    public static ConfigEvent update(String group, String key, String value) {
        return new ConfigEvent(EventType.UPDATE_CONFIG, group, key, value);
    }

    public static ConfigEvent remove(String group, String key, String value) {
        return new ConfigEvent(EventType.REMOVE_CONFIG, group, key, value);
    }

    @Override
    public String toString() {
        return "ConfigEvent{" +
                ", group='" + group + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
