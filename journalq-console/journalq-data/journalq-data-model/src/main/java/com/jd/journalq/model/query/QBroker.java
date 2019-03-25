package com.jd.journalq.model.query;

import com.jd.journalq.common.model.Query;
import com.jd.journalq.model.domain.Identity;

import java.util.List;

/**
 * Created by lining on 16-11-28.
 */
public class QBroker implements Query {

    private String retryType;
    private String ip;

    private int brokerId;

    private Identity group;

    @Deprecated
    private long brokerGroupId;

    private String keyword;

    @Deprecated
    private List<Integer> brokerGroupIds;

    private List<Integer> notInBrokerIds;

    private List<Long> inBrokerIds;

    public String getRetryType() {
        return retryType;
    }

    public void setRetryType(String retryType) {
        this.retryType = retryType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public long getBrokerGroupId() {
        return brokerGroupId;
    }

    public void setBrokerGroupId(long brokerGroupId) {
        this.brokerGroupId = brokerGroupId;
    }

    public List<Integer> getBrokerGroupIds() {
        return brokerGroupIds;
    }

    public void setBrokerGroupIds(List<Integer> brokerGroupIds) {
        this.brokerGroupIds = brokerGroupIds;
    }

    public Identity getGroup() {
        return group;
    }

    public void setGroup(Identity group) {
        this.group = group;
    }

    public List<Integer> getNotInBrokerIds() {
        return notInBrokerIds;
    }

    public void setNotInBrokerIds(List<Integer> notInBrokerIds) {
        this.notInBrokerIds = notInBrokerIds;
    }

    public List<Long> getInBrokerIds() {
        return inBrokerIds;
    }

    public void setInBrokerIds(List<Long> inBrokerIds) {
        this.inBrokerIds = inBrokerIds;
    }

}
