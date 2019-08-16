package io.chubao.joyqueue.nsr.journalkeeper.domain;

import io.chubao.joyqueue.nsr.journalkeeper.helper.Column;

/**
 * TopicDTO
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class TopicDTO extends BaseDTO {

    private String id;
    private String code;
    private String namespace;
    private Short partitions;
    @Column(alias = "priority_partitions")
    private String priorityPartitions;
    private Byte type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Short getPartitions() {
        return partitions;
    }

    public void setPartitions(Short partitions) {
        this.partitions = partitions;
    }

    public String getPriorityPartitions() {
        return priorityPartitions;
    }

    public void setPriorityPartitions(String priorityPartitions) {
        this.priorityPartitions = priorityPartitions;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }
}