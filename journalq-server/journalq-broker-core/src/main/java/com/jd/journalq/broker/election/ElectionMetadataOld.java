package com.jd.journalq.broker.election;

import com.alibaba.fastjson.JSON;
import com.jd.journalq.domain.PartitionGroup;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.jd.journalq.broker.election.ElectionNode.INVALID_NODE_ID;

public class ElectionMetadataOld {
    private PartitionGroup.ElectType electType;
    private Collection<DefaultElectionNode> allNodes;
    private Set<Integer> learners = new HashSet<>();
    private int localNodeId = INVALID_NODE_ID;
    private int leaderId = INVALID_NODE_ID;
    private int votedFor = INVALID_NODE_ID;
    private int currentTerm = 0;

    public ElectionMetadataOld() {}

    public PartitionGroup.ElectType getElectType() {
        return electType;
    }

    public void setElectType(PartitionGroup.ElectType electType) {
        this.electType = electType;
    }

    public Collection<DefaultElectionNode> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(Collection<DefaultElectionNode> allNodes) {
        this.allNodes = allNodes;
    }

    public Set<Integer> getLearners() {
        return learners;
    }

    public void setLearners(Set<Integer> learners) {
        this.learners = learners;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public int getLocalNodeId() {
        return localNodeId;
    }

    public void setLocalNodeId(int localNodeId) {
        this.localNodeId = localNodeId;
    }

    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    @Override
    public String toString() {
        return new StringBuffer("ElectionMetadata{")
                .append("electType:").append(electType)
                .append(", allNodes:").append(JSON.toJSONString(allNodes))
                .append(", learners:").append(JSON.toJSONString(learners))
                .append(", localId:").append(localNodeId)
                .append(", currentTerm:").append(currentTerm)
                .append(", votedFor:").append(votedFor)
                .append("}").toString();
    }
}
