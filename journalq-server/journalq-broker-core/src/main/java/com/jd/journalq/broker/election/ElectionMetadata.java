package com.jd.journalq.broker.election;

import com.alibaba.fastjson.JSON;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.jd.journalq.domain.PartitionGroup.ElectType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/11/24
 */
public class ElectionMetadata {
    private ElectType electType;
    private Collection<DefaultElectionNode> allNodes;
    private Set<Integer> learners = new HashSet<>();
    private int localNodeId = ElectionNode.INVALID_NODE_ID;
    private int leaderId = ElectionNode.INVALID_NODE_ID;
    private int votedFor = ElectionNode.INVALID_NODE_ID;
    private int currentTerm = 0;

    public ElectionMetadata() {}

    public ElectType getElectType() {
        return electType;
    }

    public void setElectType(ElectType electType) {
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

    public static class Build {
        private ElectionMetadata metadata = new ElectionMetadata();

        public static Build create() {
            return new Build();
        }

        public Build electionType(ElectType electType) {
            metadata.setElectType(electType);
            return this;
        }

        public Build allNodes(Collection<DefaultElectionNode> allNodes) {
            metadata.setAllNodes(allNodes);
            return this;
        }

        public Build learners(Set<Integer> learners) {
            metadata.setLearners(learners);
            return this;
        }

        public Build localNode(int localNode) {
            metadata.setLocalNodeId(localNode);
            return this;
        }

        public Build leaderId(int leaderId) {
            metadata.setLeaderId(leaderId);
            return this;
        }

        public Build currentTerm(int currentTerm) {
            metadata.setCurrentTerm(currentTerm);
            return this;
        }

        public Build votedFor(int votedFor) {
            metadata.setVotedFor(votedFor);
            return this;
        }

        public ElectionMetadata build() {
            return metadata;
        }
    }
}
