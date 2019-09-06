package io.chubao.joyqueue.nsr.journalkeeper.domain;

import io.chubao.joyqueue.nsr.journalkeeper.helper.Column;

/**
 * PartitionGroupReplicaDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class PartitionGroupReplicaDTO extends BaseDTO {

    private String id;
    private String topic;
    private String namespace;
    @Column(alias = "broker_id")
    private Long brokerId;
    private Integer group;

    public PartitionGroupReplicaDTO() {

    }

    public PartitionGroupReplicaDTO(String id, String topic, String namespace, Long brokerId, Integer group) {
        this.id = id;
        this.topic = topic;
        this.namespace = namespace;
        this.brokerId = brokerId;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}