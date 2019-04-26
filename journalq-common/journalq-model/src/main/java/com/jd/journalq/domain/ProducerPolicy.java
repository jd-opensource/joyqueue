package com.jd.journalq.domain;

import java.util.Map;
import java.util.Set;

/**
 * ProducerPolicy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/26
 */
public class ProducerPolicy {

    // 就近发送
    private Boolean nearby;
    //单线程发送
    private Boolean single;
    // 是否需要归档,默认归档
    private Boolean archive;
    // 生产者权重 <group,weight>
    private Map<String, Short> weight;
    private Set<String> blackList;
    private Integer timeOut;

    public ProducerPolicy() {

    }

    public ProducerPolicy(Boolean nearby, Boolean single, Boolean archive, Map<String, Short> weight, Set<String> blackList, Integer timeOut) {
        this.nearby = nearby;
        this.single = single;
        this.archive = archive;
        this.weight = weight;
        this.blackList = blackList;
        this.timeOut = timeOut;
    }

    public Boolean getNearby() {
        return nearby;
    }

    public void setNearby(Boolean nearby) {
        this.nearby = nearby;
    }

    public Boolean getSingle() {
        return single;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Map<String, Short> getWeight() {
        return weight;
    }

    public void setWeight(Map<String, Short> weight) {
        this.weight = weight;
    }

    public Set<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }
}