package com.jd.journalq.broker.election;

import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.election.command.AppendEntriesRequest;
import com.jd.journalq.broker.election.command.AppendEntriesResponse;
import com.jd.journalq.broker.replication.ReplicaGroup;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.store.replication.ReplicableStore;

import com.jd.journalq.toolkit.concurrent.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class FixLeaderElection extends LeaderElection {
    private static Logger logger = LoggerFactory.getLogger(FixLeaderElection.class);

    private List<DefaultElectionNode> allNodes = new LinkedList<>();

    public FixLeaderElection(TopicPartitionGroup topicPartitionGroup, ElectionConfig electionConfig,
                             ElectionManager electionManager, ClusterManager clusterManager,
                             ElectionMetadataManager metadataManager, ReplicableStore replicableStore,
                             ReplicaGroup replicaGroup, EventBus<ElectionEvent> electionEventManager,
                             int leaderId, int localNodeId, List<DefaultElectionNode> allNodes) {
        this.topicPartitionGroup = topicPartitionGroup;
        this.electionConfig = electionConfig;
        this.electionManager = electionManager;
        this.clusterManager = clusterManager;
        this.electionMetadataManager = metadataManager;
        this.replicableStore = replicableStore;
        this.replicaGroup = replicaGroup;
        this.electionEventManager = electionEventManager;
        this.leaderId = leaderId;
        this.localNodeId = localNodeId;
        this.allNodes = allNodes;
    }

    @Override
    public void doStart() throws Exception{
        super.doStart();

        if (leaderId == localNodeId) {
            becomeLeader();
        } else {
            becomeFollower();
        }

        updateElectionMetadata();

        electionEventManager.add(new ElectionEvent(ElectionEvent.Type.LEADER_FOUND, 0,
                leaderId, topicPartitionGroup));
    }

    @Override
    public void doStop() {
        replicaGroup.stop();

        super.doStop();
    }

    /**
     * 获取参与选举的所有节点
     * @return 所有节点
     */
    @Override
    public Collection<DefaultElectionNode> getAllNodes() {
        return allNodes;
    }

    @Override
    public void setLeaderId(int leaderId) throws IOException {
        if (leaderId != this.leaderId && this.leaderId != ElectionNode.INVALID_NODE_ID) {

            if (leaderId == localNodeId) {
                becomeLeader();
            } else {
                becomeFollower();
            }

            updateElectionMetadata();

            electionEventManager.add(new ElectionEvent(ElectionEvent.Type.LEADER_FOUND, 0,
                    leaderId, topicPartitionGroup));
        } else {
            this.leaderId = leaderId;
        }
    }

    /**
     * 更新选举元数据
     */
    private void updateElectionMetadata() {
        try {
            ElectionMetadata metadata = ElectionMetadata.Build.create(electionConfig.getMetadataPath(), topicPartitionGroup)
                    .electionType(PartitionGroup.ElectType.fix)
                    .allNodes(allNodes).leaderId(leaderId).localNode(localNodeId).build();
            electionMetadataManager.updateElectionMetadata(topicPartitionGroup, metadata);
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} update election metadata fail",
                    topicPartitionGroup, localNodeId, e);
        }
    }

    private void becomeLeader() {
        replicaGroup.becomeLeader(0, leaderId);

        try {
            if (!replicableStore.serviceStatus()) {
                replicableStore.enable();
            }
        } catch (Exception e) {
            logger.info("Partition group {}/node {} enable store fail, exception is {}",
                    topicPartitionGroup, leaderId, e);
        }
    }

    private void becomeFollower() {
        replicaGroup.becomeFollower(0, leaderId);

        try {
            if (replicableStore.serviceStatus()) {
                replicableStore.disable(electionConfig.getDisableStoreTimeout());
            }
        } catch (Exception e) {
            logger.info("Partition group {}/node {} disable store fail, exception is {}",
                    topicPartitionGroup, leaderId, e);
        }
    }

    @Override
    public Command handleAppendEntriesRequest(AppendEntriesRequest request) {
        if (!isStarted()) {
            logger.warn("Partition group{}/node{} receive append entries request, election not started",
                    topicPartitionGroup, localNodeId);
            return new Command(new JMQHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                    new AppendEntriesResponse.Build().success(false).build());
        }

        logger.debug("Partition group{}/node {} receive append entries request from {}",
                topicPartitionGroup, localNodeId, request.getLeaderId());

        return  replicaGroup.appendEntries(request);
    }

}
