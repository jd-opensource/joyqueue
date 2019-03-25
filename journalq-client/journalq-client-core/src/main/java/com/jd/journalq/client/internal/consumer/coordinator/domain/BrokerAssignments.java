package com.jd.journalq.client.internal.consumer.coordinator.domain;

import java.util.List;

/**
 * BrokerAssignments
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/6
 */
public class BrokerAssignments {

    private List<BrokerAssignment> assignments;

    public BrokerAssignments() {

    }

    public BrokerAssignments(List<BrokerAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<BrokerAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<BrokerAssignment> assignments) {
        this.assignments = assignments;
    }

    @Override
    public String toString() {
        return "BrokerAssignments{" +
                "assignments=" + assignments +
                '}';
    }
}