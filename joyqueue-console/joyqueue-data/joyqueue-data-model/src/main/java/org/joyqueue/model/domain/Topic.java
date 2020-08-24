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
package org.joyqueue.model.domain;

import org.joyqueue.model.domain.nsr.BaseNsrModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

import static org.joyqueue.model.domain.Namespace.DEFAULT_NAMESPACE_CODE;
import static org.joyqueue.model.domain.Namespace.DEFAULT_NAMESPACE_ID;

/**
 * 主题
 * Created by chenyanying3 on 2018-10-17
 */
public class Topic extends BaseNsrModel {
    public static final int DEFAULT_QUEUE_SIZE = 5;
    public static final int DEFAULT_SEQ_QUEUE_SIZE =1;
    public static final int TOPIC_TYPE_TOPIC = 0;
    public static final int TOPIC_TYPE_BROADCAST = 1;
    public static final int TOPIC_TYPE_SEQUENTIAL = 2;
    public static final int NEW = 2;

    /**
     * 消息类型名称
     */
    @NotNull
    private String name;
    /**
     * 消息类型代码
     */
    @Pattern(regexp = "^[a-zA-Z0-9]+[a-zA-Z0-9_]*[a-zA-Z0-9]+$", message = "Please enter correct code")
    private String code;

    /**
     * 队列数
     */
    @Min(0)
    @Max(99)
    private int partitions = DEFAULT_QUEUE_SIZE;
    /**
     * 是否归档
     */
    private boolean archive = false;
    /**
     * 类型 0:topic,1:sequential,2:broadcast,
     */
    private int type;
    /**
     * 选举方式
     */
    private int electType;

    private String labels;

    private List<String> dataCenters;

    private Namespace namespace = new Namespace(DEFAULT_NAMESPACE_ID, DEFAULT_NAMESPACE_CODE);

//    private List<BrokerGroup> brokerGroups;
    private BrokerGroup brokerGroup;

    private List<Broker> brokers;

    @Min(0)
    @Max(20)
    private int brokerNum ;

    /**
     * 副本数量(默认3个，表示主+从，总共3个)
     */
    private int replica = 3;

    private org.joyqueue.domain.Topic.TopicPolicy policy;

    public Topic() {

    }

    public Topic(String code) {
        this.code = code;
    }

    public Topic(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public int getElectType() {
        return electType;
    }

    public void setElectType(int electType) {
        this.electType = electType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

//    public List<BrokerGroup> getBrokerGroups() {
//        return brokerGroups;
//    }
//
//    public void setBrokerGroups(List<BrokerGroup> brokerGroups) {
//        this.brokerGroups = brokerGroups;
//    }


    public BrokerGroup getBrokerGroup() {
        return brokerGroup;
    }

    public void setBrokerGroup(BrokerGroup brokerGroup) {
        this.brokerGroup = brokerGroup;
    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<Broker> brokers) {
        this.brokers = brokers;
    }

    public int getBrokerNum() {
        return brokerNum;
    }

    public void setBrokerNum(int brokerNum) {
        this.brokerNum = brokerNum;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public int getReplica() {
        return replica;
    }

    public void setReplica(int replica) {
        this.replica = replica;
    }

    public List<String> getDataCenters() {
        return dataCenters;
    }

    public void setDataCenters(List<String> dataCenters) {
        this.dataCenters = dataCenters;
    }

    public org.joyqueue.domain.Topic.TopicPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(org.joyqueue.domain.Topic.TopicPolicy policy) {
        this.policy = policy;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", partitions=" + partitions +
                ", archive=" + archive +
                ", type=" + type +
                ", electType=" + electType +
                ", labels='" + labels + '\'' +
                ", namespace=" + namespace +
                ", brokerGroup=" + brokerGroup +
                ", brokers=" + brokers +
                ", brokerNum=" + brokerNum +
                ", replica=" + replica +
                '}';
    }
}