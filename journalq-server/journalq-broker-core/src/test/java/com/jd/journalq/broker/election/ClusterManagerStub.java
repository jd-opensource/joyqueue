package com.jd.journalq.broker.election;

import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.common.domain.TopicName;

import java.util.Set;

/**
 * Created by zhuduohui on 2018/10/19.
 */
public class ClusterManagerStub extends ClusterManager {
    public ClusterManagerStub() {
        super(null, null,null);
    }

    @Override
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, Integer termId) {
    }
}