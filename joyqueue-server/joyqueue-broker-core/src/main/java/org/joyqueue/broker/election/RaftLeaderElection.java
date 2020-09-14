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
package org.joyqueue.broker.election;

import com.alibaba.fastjson.JSON;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.election.command.AppendEntriesRequest;
import org.joyqueue.broker.election.command.AppendEntriesResponse;
import org.joyqueue.broker.election.command.TimeoutNowRequest;
import org.joyqueue.broker.election.command.TimeoutNowResponse;
import org.joyqueue.broker.election.command.VoteRequest;
import org.joyqueue.broker.election.command.VoteResponse;
import org.joyqueue.broker.replication.ReplicaGroup;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.joyqueue.broker.election.ElectionEvent.Type.LEADER_FOUND;
import static org.joyqueue.broker.election.ElectionEvent.Type.START_ELECTION;
import static org.joyqueue.broker.election.ElectionNode.INVALID_NODE_ID;
import static org.joyqueue.broker.election.ElectionNode.State.CONDIDATE;
import static org.joyqueue.broker.election.ElectionNode.State.FOLLOWER;
import static org.joyqueue.broker.election.ElectionNode.State.LEADER;
import static org.joyqueue.broker.election.ElectionNode.State.TRANSFERRING;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class RaftLeaderElection extends LeaderElection  {
    private final int INVALID_VOTE_FOR = -1;

    private static Logger logger = LoggerFactory.getLogger(RaftLeaderElection.class);

    private Map<Integer, DefaultElectionNode> allNodes = new ConcurrentHashMap<>();
    private ElectionNode localNode;
    private Set<Integer> learners;

    private int currentTerm;
    private int votedFor = INVALID_VOTE_FOR;
    private int transferee = INVALID_NODE_ID;

    private ScheduledExecutorService electionTimerExecutor;
    private ScheduledFuture electionTimerFuture;
    private ScheduledFuture voteTimerFuture;
    private ScheduledFuture heartbeatTimerFuture;
    private ScheduledFuture transferLeaderTimerFuture;
    private ScheduledFuture reportLeaderFuture;
    private ScheduledFuture leaderRebalanceFuture;
    private ExecutorService electionExecutor;

    private long lastRebalanceTime;

    RaftLeaderElection(TopicPartitionGroup topicPartitionGroup, ElectionConfig electionConfig,
                       ElectionManager electionManager, ClusterManager clusterManager,
                       ElectionMetadataManager metadataManager, ReplicableStore replicableStore,
                       ReplicaGroup replicaGroup, ScheduledExecutorService electionTimerExecutor,
                       ExecutorService electionExecutor, EventBus<ElectionEvent> electionEventManager,
                       int localNodeId, List<DefaultElectionNode> allNodes,
                       Set<Integer> learners) {
        this.topicPartitionGroup = topicPartitionGroup;
        this.electionConfig = electionConfig;
        this.electionManager = electionManager;
        this.clusterManager = clusterManager;
        this.electionMetadataManager = metadataManager;
        this.replicableStore = replicableStore;
        this.replicaGroup = replicaGroup;
        this.electionTimerExecutor = electionTimerExecutor;
        this.electionExecutor = electionExecutor;
        this.electionEventManager = electionEventManager;
        this.localNodeId = localNodeId;
        this.learners = learners;

        setAllNodes(allNodes, learners);
        this.localNode = getNode(localNodeId);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        ElectionMetadata metadata = electionMetadataManager.getElectionMetadata(topicPartitionGroup);
        if (metadata != null) {
            currentTerm = metadata.getCurrentTerm();
            votedFor = metadata.getVotedFor();
        } else {
            updateElectionMetadata();
        }

        resetElectionTimer();

        leaderRebalanceFuture = electionTimerExecutor.scheduleAtFixedRate(this::rebalanceLeader,
                60 + new Random().nextInt(60), 60, TimeUnit.SECONDS);

        reportLeaderFuture = electionTimerExecutor.scheduleAtFixedRate(this::reportLeadership,
                60 + new Random().nextInt(60), 60, TimeUnit.SECONDS);

        logger.info("Raft leader election of {}, local node {}, all node {} started, " +
                    "term is {}, vote for is {}",
                topicPartitionGroup, localNode, JSON.toJSONString(allNodes), currentTerm, votedFor);
    }

    @Override
    protected void doStop() {
        cancelElectionTimer();
        cancelHeartbeatTimer();
        cancelTransferLeaderTimer();

        nodeOffline(currentTerm);
        replicaGroup.stop();

        if (leaderRebalanceFuture != null) {
            leaderRebalanceFuture.cancel(true);
        }
        if (reportLeaderFuture != null) {
            reportLeaderFuture.cancel(true);
        }

        super.doStop();

        logger.info("Raft leader election of partition group {}/node {} stoped",
                topicPartitionGroup, localNode);
    }

    /**
     * 获取参与选举的所有节点
     * @return 全部节点
     */
    @Override
    public Collection<DefaultElectionNode> getAllNodes() {
        return allNodes.values();
    }

    /**
     * 根据id获取节点
     * @param nodeId 节点id
     * @return 选举节点
     */
    private ElectionNode getNode(int nodeId) {
        return allNodes.get(nodeId);
    }

    /**
     * 获取学习节点
     * @return 学习节点
     */
    private Set<Integer> getLearners() {
        return learners;
    }

    /**
     * 设置参与选举的所有节点
     * @param allNodes 所有节点
     * @param learners 学习节点，不参与选举
     */
    private void setAllNodes(List<DefaultElectionNode> allNodes, Set<Integer> learners) {
        allNodes.stream().filter(n -> !learners.contains(n.getNodeId()))
                .forEach((n) -> {
                        n.setState(FOLLOWER);
                        n.setVoteGranted(false);
                        this.allNodes.put(n.getNodeId(), n);
        });
    }

    public ElectionNode.State state() {
        return localNode.getState();
    }

    public void state(ElectionNode.State state) {
        localNode.setState(state);
    }

    /**
     * 更新选举元数据
     * 每次重新选举或者成员变更时执行
     */
    private void updateElectionMetadata() {
        try (ElectionMetadata metadata = ElectionMetadata.Build.create(electionConfig.getMetadataPath(), topicPartitionGroup)
                .electionType(PartitionGroup.ElectType.raft)
                .allNodes(getAllNodes()).learners(getLearners()).leaderId(leaderId)
                .localNode(localNodeId).currentTerm(currentTerm).votedFor(votedFor)
                .build()) {
            logger.info("Partition group {}/node {} update metadata of {}",
                    topicPartitionGroup, localNode, metadata);
            electionMetadataManager.updateElectionMetadata(topicPartitionGroup, metadata);
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} update election metadata fail",
                    topicPartitionGroup, localNode, e);
        }
    }

    /**
     * 增加参与选举的节点
     * @param node 增加的节点
     */
    @Override
    public void addNode(DefaultElectionNode node) throws ElectionException {
        allNodes.put(node.getNodeId(), node);
        updateElectionMetadata();

        super.addNode(node);
    }

    /**
     * 删除参与选举的节点
     * @param nodeId 删除的节点Id
     */
    @Override
    public void removeNode(int nodeId) {
        allNodes.remove(nodeId);
        updateElectionMetadata();

        super.removeNode(nodeId);
    }

    /**
     * 改变leader
     * @param leaderId 设置的leader id
     * @throws Exception exception
     */
    @Override
    public void setLeaderId(int leaderId) throws Exception {
        if (leaderId != INVALID_NODE_ID && this.leaderId != INVALID_NODE_ID) {
            transferLeadership(leaderId);
        }
    }

    /**
     * 切换状态
     * @param state 切换到的状态
     */
    private void transitionTo(ElectionNode.State state) {
        localNode.setState(state);
        replicaGroup.setState(state);
    }

    /**
     * 获取最后一条log的term
     * @return 最后一条log的term
     */
    private int getLastLogTerm() {
        return replicableStore.lastEntryTerm();

    }

    /**
     * 获取最后一条log的position
     * @return 最后一条log的position
     */
    private long getLastLogPosition() {
        return replicableStore.rightPosition();
    }

    /**
     * 重置定时器，如果定时器超时则启动新一轮选举
     */
    private synchronized void resetElectionTimer() {
        if (electionTimerFuture != null && !electionTimerFuture.isDone()) {
            electionTimerFuture.cancel(true);
            electionTimerFuture = null;
        }
        electionTimerFuture = electionTimerExecutor.schedule(this::handleElectionTimeout,
                getElectionTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel the election timer
     */
    private synchronized void cancelElectionTimer() {
        if (electionTimerFuture != null && !electionTimerFuture.isDone()) {
            electionTimerFuture.cancel(true);
            electionTimerFuture = null;
        }
    }

    private int getElectionTimeoutMs() {
        Random random = new Random();
        return electionConfig.getElectionTimeout() + random.nextInt(electionConfig.getElectionTimeout());

    }

    private synchronized void resetVoteTimer() {
        if (voteTimerFuture != null && !voteTimerFuture.isDone()) {
            voteTimerFuture.cancel(true);
            voteTimerFuture = null;
        }
        voteTimerFuture = electionTimerExecutor.schedule(this::handleVoteTimeout,
                electionConfig.getVoteTimeout(), TimeUnit.MILLISECONDS);
    }

    private synchronized void cancelVoteTimer() {
        if (voteTimerFuture != null && !voteTimerFuture.isDone()) {
            voteTimerFuture.cancel(true);
            voteTimerFuture = null;
        }
    }

    /**
     * election 定时器超时，向其他节点发送preVote命令
     */
    private synchronized void handleElectionTimeout() {
        if (!isStarted()) {
            throw new IllegalStateException("Election timeout, election service not start");
        }

        if (electionTimerFuture == null) {
            logger.info("Partition group {}/node {} election timeout, timer future is null",
                    topicPartitionGroup, localNode);
        }

        /*
         * 如果只有一个节点，直接设置该节点为leader
         */
        if (getAllNodes().size() == 1) {
            becomeLeader();
            return;
        }

        if (state() != FOLLOWER) {
            logger.info("Partition group {}/node {} election timeout, state is {}",
                    topicPartitionGroup, localNode, state());
			if (state() == LEADER) {
				return;
			}
        }

        logger.info("Partition group {}/node {} election timeout, current term is {}.",
                topicPartitionGroup, localNode, currentTerm);

		leaderId = INVALID_NODE_ID;
		
        try {
            preVote();
        } catch (Throwable t) {
            logger.warn("Partition group {}/node {} preVote fail",
                    topicPartitionGroup, localNode);
        }
        resetElectionTimer();
    }

    /**
     * 向其他节点发送preVote命令，如果本节点收到多数投票才发起正式选举
     */
    private void preVote() {
        localNode.setVoteGranted(true);

        int lastLogTerm = getLastLogTerm();
        long lastLogPos = getLastLogPosition();

        for (ElectionNode node : getAllNodes()) {
            if (node.equals(localNode)) {
                continue;
            }

            electionExecutor.submit(() -> {
                node.setVoteGranted(false);
                VoteRequest voteRequest = new VoteRequest(topicPartitionGroup, currentTerm, localNodeId,
                        lastLogTerm, lastLogPos, true);
                JoyQueueHeader header = new JoyQueueHeader(Direction.REQUEST, CommandType.RAFT_VOTE_REQUEST);
                Command command = new Command(header, voteRequest);

                logger.info("Partition group {}/node{} send prevote request to node {}",
                        topicPartitionGroup, localNode, node);

                try {
                    electionManager.sendCommand(node.getAddress(), command,
                            electionConfig.getSendCommandTimeout(), new VoteRequestCallback(currentTerm, node));
                } catch (Exception e) {
                    logger.info("Partition group {}/node{} send pre vote request to node {} fail",
                            topicPartitionGroup, localNode, node, e);
                }
            });
        }
    }

    /**
     * 选举自己为候选者，然后向其他节点发送投票请求
     */
    private void electSelf() {
        currentTerm++;
        transitionTo(CONDIDATE);
        leaderId = INVALID_NODE_ID;
        votedFor = localNode.getNodeId();
        localNode.setVoteGranted(true);

        nodeOffline(currentTerm);

        updateElectionMetadata();
        electionEventManager.add(new ElectionEvent(START_ELECTION,
                currentTerm, INVALID_NODE_ID, topicPartitionGroup));

        resetVoteTimer();

        int lastLogTerm = getLastLogTerm();
        long lastLogPos = getLastLogPosition();

        for (ElectionNode node : getAllNodes()) {
            if (node.equals(localNode)) {
                continue;
            }

            electionExecutor.submit(() -> {
                node.setVoteGranted(false);
                VoteRequest voteRequest = new VoteRequest(topicPartitionGroup, currentTerm, localNodeId,
                        lastLogTerm, lastLogPos, false);
                JoyQueueHeader header = new JoyQueueHeader(Direction.REQUEST, CommandType.RAFT_VOTE_REQUEST);
                Command command = new Command(header, voteRequest);

                logger.info("Partition group {}/node{} send vote request to node {}",
                        topicPartitionGroup, localNode, node);

                try {
                    electionManager.sendCommand(node.getAddress(), command,
                            electionConfig.getSendCommandTimeout(), new VoteRequestCallback(currentTerm, node));
                } catch (Exception e) {
                    logger.info("Partition group {}/node{} send vote request to node {} fail",
                            topicPartitionGroup, localNode, node, e);
                }
            });
        }
    }

    /**
     * Callback of send vote request command
     */
    private class VoteRequestCallback implements CommandCallback {
        private int term;
        private ElectionNode node;

        VoteRequestCallback(int term, ElectionNode node) {
            this.term = term;
            this.node = node;
        }

        @Override
        public void onSuccess(Command request, Command responseCommand) {
            if (!(request.getPayload() instanceof VoteRequest)) {
                return;
            }

            VoteRequest voteRequest = (VoteRequest)request.getPayload();
            if (currentTerm != term) {
                logger.info("Partition group {}/node {} receive vote response from {}, " +
                            "current term is {}, term is {}",
                        topicPartitionGroup, localNode, node.getNodeId(), currentTerm, term);
                return;
            }

            try {
                if (voteRequest.isPreVote()) {
                    handlePreVoteResponse(responseCommand);
                } else {
                    handleVoteResponse(responseCommand);
                }
            } catch (Throwable t) {
                logger.warn("Partition group {}/node {} handle vote response fail",
                        topicPartitionGroup, localNode);
            }
        }

        @Override
        public void onException(Command request, Throwable cause) {
            logger.info("Partition group {}/node {} send vote request to {} failed",
                    topicPartitionGroup, localNode, node.getNodeId(), cause);
        }
    }

    /**
     * 投票定时器超时，重新进行pre vote
     */
    private synchronized void handleVoteTimeout() {
        if (!isStarted()) {
            throw new IllegalStateException("Vote timeout, election service not start");
        }

        if (voteTimerFuture == null) {
            logger.info("Partition group {}/node {} vote timeout, timer future is null",
                    topicPartitionGroup, localNode);
        }

        logger.info("Partition group {}/node {} vote timeout, term is {}, state is {}",
                topicPartitionGroup, localNode, currentTerm, state());

        if (state() != CONDIDATE) {
            return;
        }

        stepDown(currentTerm);
    }

    /**
     * 处理投票请求
     * @param voteRequest 投票请求
     * @return 返回的命令
     */
    public synchronized Command handleVoteRequest(VoteRequest voteRequest){
        boolean voteGranted = false;

        if (voteRequest == null) {
            logger.warn("Partition group {}/node{} receive vote request, request is null",
                    topicPartitionGroup, localNode);
            return null;
        }

        if (!isStarted()) {
            logger.warn("Partition group {}/node{} receive vote request, election not started",
                    topicPartitionGroup, localNode);
            return null;
        }

        if (!allNodes.containsKey(voteRequest.getCandidateId())) {
            logger.warn("Partition group {}/node{} receive pre vote request from unknown node {}",
                    topicPartitionGroup, localNode, voteRequest.getCandidateId());
            return null;
        }

        if (currentTerm > voteRequest.getTerm()) {
            logger.info("Partition group {}/node{} receive vote request from {}, currentTerm {} is " +
                            "great than request term {}",
                    topicPartitionGroup, localNode, voteRequest.getCandidateId(), currentTerm, voteRequest.getTerm());
            return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_VOTE_RESPONSE),
                    new VoteResponse(currentTerm, voteRequest.getCandidateId(), localNodeId, voteGranted));
        }

        /*
         * 如果当前term小于投票请求的term，首先当前节点降为FOLLOWER，然后参与选举
         */
        if (currentTerm < voteRequest.getTerm()) {
            logger.info("Partition group {}/node{} receive vote request from {}, currentTerm {} is " +
                            "less than request term {}",
                    topicPartitionGroup, localNode, voteRequest.getCandidateId(), currentTerm, voteRequest.getTerm());
            stepDown(voteRequest.getTerm());
        }

        int lastLogTerm = getLastLogTerm();
        long lastLogPos = getLastLogPosition();

        logger.info("Partition group {}/node{} receive vote request from {}, lastLogTerm is {}, lastLogIndex is {}, " +
                        "request lastLogTerm is {}, request lastLogIndex is {}, voteFor is {}, request candidateId is {}",
                topicPartitionGroup, localNode, voteRequest.getCandidateId(), lastLogTerm, lastLogPos,
                voteRequest.getLastLogTerm(), voteRequest.getLastLogPos(), votedFor, voteRequest.getCandidateId());

        if (lastLogTerm < voteRequest.getLastLogTerm() ||
                (lastLogTerm == voteRequest.getLastLogTerm() && lastLogPos <= voteRequest.getLastLogPos())) {
            if (votedFor == INVALID_VOTE_FOR || votedFor == voteRequest.getCandidateId()) {
                votedFor = voteRequest.getCandidateId();
                updateElectionMetadata();
                voteGranted = true;
                stepDown(voteRequest.getTerm());
            }
        }

        return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_VOTE_RESPONSE),
                           new VoteResponse(currentTerm, voteRequest.getCandidateId(), localNodeId, voteGranted));
    }

    /**
     * 处理预投票请求
     * @param voteRequest 预投票请求
     * @return 返回的命令
     */
    public synchronized Command handlePreVoteRequest(VoteRequest voteRequest){
        boolean voteGranted = false;

        if (voteRequest == null) {
            logger.warn("Partition group {}/node{} receive pre vote request, request is null",
                    topicPartitionGroup, localNode);
            return null;
        }

        if (!isStarted()) {
            logger.warn("Partition group {}/node{} receive pre vote request, election not started",
                    topicPartitionGroup, localNode);
            return null;
        }

        if (!allNodes.containsKey(voteRequest.getCandidateId())) {
            logger.warn("Partition group {}/node{} receive pre vote request from unknown node {}",
                    topicPartitionGroup, localNode, voteRequest.getCandidateId());
            return null;
        }

        if (currentTerm > voteRequest.getTerm()) {
            logger.info("Partition group {}/node{} receive pre vote request from {}, currentTerm {} is " +
                            "great than request term {}",
                    topicPartitionGroup, localNode, voteRequest.getCandidateId(), currentTerm, voteRequest.getTerm());
            return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_VOTE_RESPONSE),
                    new VoteResponse(currentTerm, voteRequest.getCandidateId(), localNodeId, voteGranted));
        }

        int lastLogTerm = getLastLogTerm();
        long lastLogPos = getLastLogPosition();

        logger.info("Partition group {}/node{} receive pre vote request from {}, lastLogTerm is {}, " +
                        "lastLogIndex is {}, request lastLogTerm is {}, request lastLogIndex is {}, " +
                        "voteFor is {}, request term is {}",
                topicPartitionGroup, localNode, voteRequest.getCandidateId(), lastLogTerm, lastLogPos,
                voteRequest.getLastLogTerm(), voteRequest.getLastLogPos(), votedFor, voteRequest.getTerm());

        if (lastLogTerm < voteRequest.getLastLogTerm() ||
                (lastLogTerm == voteRequest.getLastLogTerm() && lastLogPos <= voteRequest.getLastLogPos())) {
            voteGranted = true;
        }

        return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_VOTE_RESPONSE),
                           new VoteResponse(currentTerm, voteRequest.getCandidateId(), localNodeId, voteGranted));
    }

    /**
     * 处理选举投票响应命令，如果当前term小于响应中的term，当前节点降为Follower
     * 如果当前节点获得的投票数大于半数，则当前节点成为Leader
     * @param command 投票响应命令
     */
    private synchronized void handleVoteResponse(Command command) {
        if (command == null) {
            logger.warn("Partition group {}/node{} receive vote response is null",
                    topicPartitionGroup, localNode);
            return;
        }

        if (!(command.getPayload() instanceof VoteResponse)) {
            logger.info("Partition group {}/node{} receive vote response object type error",
                    topicPartitionGroup, localNode);
            return;
        }
        VoteResponse voteResponse = (VoteResponse)command.getPayload();

        logger.info("Partition group {}/node{} receive vote response from {}, term is {}, " +
                    "vote candidateId is {}, vote granted is {}",
                topicPartitionGroup, localNode, voteResponse.getVoteNodeId(), voteResponse.getTerm(),
                voteResponse.getCandidateId(), voteResponse.isVoteGranted());
        if (state() != CONDIDATE) {
            logger.warn("Partition group {}/node{} receive vote response, local node state is {}",
                    topicPartitionGroup, localNode, state());
            return;
        }

        if (voteResponse.getTerm() > currentTerm) {
            logger.info("Partition group {}/node{} receive vote response, current term is {}, " +
                        "response term is {}",
                    topicPartitionGroup, localNode, currentTerm, voteResponse.getTerm());
            stepDown(voteResponse.getTerm());
        }

        ElectionNode voteNode = getNode(voteResponse.getVoteNodeId());
        voteNode.setVoteGranted(voteResponse.isVoteGranted());
        int voteGranted = 0;
        for (ElectionNode node : getAllNodes()) {
            logger.info("Partition group {}/node {} voteGranted is {}",
                    topicPartitionGroup, node, node.isVoteGranted());
            if (node.isVoteGranted()) {
                voteGranted++;
            }
        }

        logger.info("Partition group {}/node {} receive {} votes",
                topicPartitionGroup, localNode, voteGranted);

        // if granted quorum, become leader
        if (voteGranted > (getAllNodes().size()) / 2) {
            logger.info("Partition group {}/node{} receive {} votes, become leader term is {}.",
                    topicPartitionGroup, localNode, voteGranted, currentTerm);
            becomeLeader();
        }
    }

    /**
     * 处理预选举预投票响应命令，如果当前term小于响应中的term，当前节点降为Follower
     * 如果当前节点获得的投票数大于半数，则触发真正的选举
     * @param command 预投票响应命令
     */
    private synchronized void handlePreVoteResponse(Command command) {
        if (command == null) {
            logger.warn("Partition group {}/node{} receive pre vote response is null",
                    topicPartitionGroup, localNode);
            return;
        }

        if (!(command.getPayload() instanceof VoteResponse)) {
            logger.info("Partition group {}/node{} receive pre vote response object type error",
                    topicPartitionGroup, localNode);
            return;
        }
        VoteResponse voteResponse = (VoteResponse)command.getPayload();

        logger.info("Partition group {}/node{} receive pre vote response from {}, term is {}, " +
                    "vote candidateId is {}, vote granted is {}",
                topicPartitionGroup, localNode, voteResponse.getVoteNodeId(), voteResponse.getTerm(),
                voteResponse.getCandidateId(), voteResponse.isVoteGranted());

        if (state() != FOLLOWER) {
            logger.info("Partition group {}/node {} receive pre vote response, state is {}",
                    topicPartitionGroup, localNode, state());
            return;
        }
        if (voteResponse.getTerm() > currentTerm) {
            logger.info("Partition group {}/node{} receive pre vote response, current term is {}, " +
                        "response term is {}",
                    topicPartitionGroup, localNode, currentTerm, voteResponse.getTerm());
            stepDown(voteResponse.getTerm());
            return;
        }

        ElectionNode voteNode = getNode(voteResponse.getVoteNodeId());
        voteNode.setVoteGranted(voteResponse.isVoteGranted());
        int voteGranted = 0;
        for (ElectionNode node : getAllNodes()) {
            logger.info("Partition group {}/node {} pre vote voteGranted is {}",
                    topicPartitionGroup, node, node.isVoteGranted());
            if (node.isVoteGranted()) {
                voteGranted++;
            }
        }

        logger.info("Partition group {}/node {} receive {} pre votes",
                topicPartitionGroup, localNode, voteGranted);

        // if granted quorum, become leader
        if (voteGranted > (getAllNodes().size()) / 2) {
            logger.info("Partition group {}/node{} receive {} pre votes, start vote, term is {}.",
                    topicPartitionGroup, localNode, voteGranted, currentTerm);
            electSelf();
        }
    }

    /**
     * 处理添加记录请求
     * @param request 添加记录请求
     * @return 返回命令
     */
    @Override
    public synchronized Command handleAppendEntriesRequest(AppendEntriesRequest request) {
        if (!isStarted()) {
            logger.warn("Partition group {}/node{} receive append entries request, election not started",
                    topicPartitionGroup, localNode);
            return null;
        }

        logger.debug("Partition group {}/node {} receive append entries request, currentTerm is {}, " +
                    "request term is {}, leaderId is {}",
                topicPartitionGroup, localNode, currentTerm, request.getTerm(), request.getLeaderId());

        if (!allNodes.containsKey(request.getLeaderId())) {
            logger.warn("Partition group {}/node{} receive append entries request from unknown node {}",
                    topicPartitionGroup, localNode, request.getLeaderId());
            return null;
        }

        if (request.getTerm() < currentTerm) {
            logger.info("Partition group {}/node {} receive append entries request from {}, current term {} " +
                            "is bigger than request term {}, length is {}",
                    topicPartitionGroup, localNode, currentTerm, request.getLeaderId(),
                    request.getTerm(), request.getEntriesLength());
            return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                    new AppendEntriesResponse.Build().success(false).term(currentTerm)
                            .nextPosition(request.getStartPosition()).build());
        }

        checkStepDown(request.getTerm(), request.getLeaderId());

        resetElectionTimer();

        if (request.getEntries() != null && request.getEntries().hasRemaining()) {
            return replicaGroup.appendEntries(request);
        } else {
            // as heartbeat
            return new Command(new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE),
                    new AppendEntriesResponse.Build().topicPartitionGroup(topicPartitionGroup)
                    .term(currentTerm).writePosition(replicableStore.rightPosition()).nextPosition(replicableStore.rightPosition())
                    .replicaId(localNodeId).success(true).entriesTerm(request.getEntriesTerm())
                    .build());
        }
    }

    private synchronized void maybeStartNewHeartbeat() {
        if (electionConfig.enableSharedHeartbeat()) {
            startNewHeartbeat();
        } else {
            resetHeartbeatTimer();
        }
    }

    /**
     * 开始新一轮心跳，向Follower节点发送心跳命令，重置心跳定时器
     */
    private synchronized void startNewHeartbeat() {
        if (!isStarted()) {
            throw new IllegalStateException("Start new heartbeat leader, election service not start");
        }

        if (!isLeader() && state() != TRANSFERRING) {
            logger.info("Partition group {}/node {} start new heartbeat, state is {}",
                    topicPartitionGroup, localNode, state());
            return;
        }

        AppendEntriesRequest appendEntriesRequest = AppendEntriesRequest.Build.create()
                .partitionGroup(topicPartitionGroup).term(currentTerm).leader(leaderId).build();
        for (ElectionNode node : getAllNodes()) {
            if (node.equals(localNode)) {
                continue;
            }
            try {
                electionExecutor.submit(() -> {
                    JoyQueueHeader header = new JoyQueueHeader(Direction.REQUEST, CommandType.RAFT_APPEND_ENTRIES_REQUEST);
                    Command command = new Command(header, appendEntriesRequest);

                    logger.debug("Partition group {}/node{} send heartbeat request {} to {}",
                            topicPartitionGroup, localNode, appendEntriesRequest, node.getNodeId());
                    try {
                        electionManager.sendCommand(node.getAddress(), command,
                                electionConfig.getSendCommandTimeout(), new HeartbeatRequestCallback(node));
                    } catch (Exception e) {
                        logger.warn("Partition group {}/node{} send heartbeat to {} fail",
                                topicPartitionGroup, localNode, node, e);
                    }
                });
            } catch (Exception e) {
                logger.warn("Partition group {}/node {} submit new heartbeat task fail",
                        topicPartitionGroup, localNode, e);
            }
        }
        resetHeartbeatTimer();
    }


    /**
     * Callback of send heartbeat request command
     */
    private class HeartbeatRequestCallback implements CommandCallback {
        private ElectionNode node;

        HeartbeatRequestCallback(ElectionNode node) {
            this.node = node;
        }

        @Override
        public void onSuccess(Command request, Command responseCommand) {
            handleHeartbeatResponse(responseCommand, node);
        }

        @Override
        public void onException(Command request, Throwable cause) {
            logger.info("Partition group {}/node {} send heartbeat request to {} failed",
                    topicPartitionGroup, localNode, node, cause);
        }
    }

    /**
     * 处理心跳响应消息
     * @param command 心跳响应命令
     */
    private synchronized void handleHeartbeatResponse(Command command, ElectionNode node) {
        if (command == null) {
            logger.warn("Partition group {}/node{} receive heartbeat response is null",
                    topicPartitionGroup, localNode);
            return;
        }

        if (!(command.getPayload() instanceof AppendEntriesResponse)) {
            logger.info("Partition group {}/node{} receive append entries response object type error",
                    topicPartitionGroup, localNode);
            return;
        }
        AppendEntriesResponse response = (AppendEntriesResponse)command.getPayload();
        logger.debug("Partition group {}/node{} receive heartbeat response from {}, term is {}",
                topicPartitionGroup, localNode, node.getNodeId(), response.getTerm());

        if (response.getTerm() > currentTerm) {
            logger.info("Partition group{}/node{} receive heartbeat response from {}, " +
                            "response term {} is greater than current term {}",
                    topicPartitionGroup, localNode, node, response.getTerm(), currentTerm);
            stepDown(response.getTerm());
        }
    }

    /**
     * 重置心跳定时器
     */
    private synchronized void cancelHeartbeatTimer() {
        if (heartbeatTimerFuture != null && !heartbeatTimerFuture.isDone()) {
            heartbeatTimerFuture.cancel(true);
            heartbeatTimerFuture = null;
        }
    }

    /**
     * 重置心跳定时器
     */
    private synchronized void resetHeartbeatTimer() {
        if (heartbeatTimerFuture != null && !heartbeatTimerFuture.isDone()) {
            heartbeatTimerFuture.cancel(true);
            heartbeatTimerFuture = null;
        }
        heartbeatTimerFuture = electionTimerExecutor.schedule(this::maybeStartNewHeartbeat,
                electionConfig.getHeartbeatTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * 本地节点成为leader，设置状态，停止投票定时器，发送选举事件，向follower发送心跳
     */
    private synchronized void becomeLeader() {
        transitionTo(LEADER);
        leaderId = localNode.getNodeId();

        getAllNodes().forEach(node -> {
            if (!node.equals(localNode)) {
                node.setState(FOLLOWER);
            }
            node.setVoteGranted(false);
        });

        startNewHeartbeat();

        try {
            replicaGroup.becomeLeader(currentTerm, leaderId);
            nodeOnline(currentTerm);

            updateElectionMetadata();
            updateMetadata(leaderId, currentTerm);
            electionEventManager.add(new ElectionEvent(LEADER_FOUND,
                    currentTerm, localNode.getNodeId(), topicPartitionGroup));
            cancelElectionTimer();
            cancelVoteTimer();
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} as leader fail",
                    topicPartitionGroup, localNode, e);
        }
        
    }

    /**
     * 本地节点成为FOLLOWER，设置状态，停止心跳定时器
     */
    private synchronized void becomeFollower(int leaderId, int term) {
        logger.info("Partition group {}/node{} become follower, leaderId is {}" +
                    ", term is {}",
                topicPartitionGroup, localNode, leaderId, term);

        transitionTo(FOLLOWER);
        this.leaderId = leaderId;
        this.currentTerm = term;

        getAllNodes().forEach((node) -> {
            node.setState(node.getNodeId() == leaderId ? LEADER : FOLLOWER);
            node.setVoteGranted(false);
        });

        replicaGroup.becomeFollower(currentTerm, leaderId);

        nodeOffline(term);

        updateElectionMetadata();
        updateMetadata(leaderId, currentTerm);
        electionEventManager.add(new ElectionEvent(LEADER_FOUND,
                currentTerm, leaderId, topicPartitionGroup));

        cancelHeartbeatTimer();
    }

    private void checkStepDown(int requestTerm, int requestLeaderId) {
        if (currentTerm < requestTerm) {
            logger.info("Partition group {}/node {} receive heartbeat from new leader {}" +
                        " with higher term {}",
                    topicPartitionGroup, localNode, requestLeaderId, requestTerm);
            stepDown(requestTerm);
        } else if (state() != FOLLOWER) {
            logger.info("Partition group {}/node {} receive heartbeat from leader {}",
                    topicPartitionGroup, localNode, requestLeaderId);
            stepDown(requestTerm);
        } else if (leaderId == INVALID_NODE_ID) {
            logger.info("Partition group {}/node {} receive heartbeat from new leader {}",
                    topicPartitionGroup, localNode, requestLeaderId);
            stepDown(requestTerm);
        } else if (leaderId != requestLeaderId) {
            logger.info("Partition group {}/node {} receive heartbeat from another leader {}",
                    topicPartitionGroup, localNode, leaderId);
            leaderId = requestLeaderId;
        }
        if (leaderId == INVALID_NODE_ID) {
            becomeFollower(requestLeaderId, requestTerm);
        }
    }

    /**
     * 本地节点因为某些原因退位，比如收到VoteRequest中的term大于本地term
     * 或者transfer leader到另外的节点
     * @param term 任期
     */
    @Override
    public synchronized void stepDown(int term){
        logger.info("Partition group {}/node {} step down, term is {}, " +
                    "current term is {}, vote for is {}",
                topicPartitionGroup, localNode, term, currentTerm, votedFor);

        leaderId = INVALID_NODE_ID;
        if (term > currentTerm) {
            votedFor = INVALID_VOTE_FOR;
            currentTerm = term;
            updateElectionMetadata();
        }

        nodeOffline(term);

        switch(state()) {
            case CONDIDATE:
                cancelElectionTimer();
                cancelVoteTimer();
                break;
            case LEADER:
                updateMetadata(leaderId, currentTerm);
                replicaGroup.becomeFollower(currentTerm, leaderId);
                cancelHeartbeatTimer();
                break;
            default:
                break;
        }

        if (transferee != INVALID_NODE_ID) {
            transferee = INVALID_NODE_ID;
            cancelTransferLeaderTimer();
        }

        transitionTo(FOLLOWER);
        resetElectionTimer();
    }

    private synchronized void nodeOffline(int term) {
        long startTime = SystemClock.now();

        try {
            if (replicableStore.serviceStatus()) {
                replicableStore.disable();
            }
            replicableStore.term(term);
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} disable store fail",
                    topicPartitionGroup, localNode, e);
        }

        logger.info("Partition group {}/node {} disable store elapse {} ms",
                topicPartitionGroup, localNode, SystemClock.now() - startTime);
    }

    private void maybeNodeOnline() {
        if (!replicableStore.serviceStatus()) {
            replicableStore.enable();
        }
    }

    private synchronized void nodeOnline(int term) {
        long startTime = SystemClock.now();

        try {
            if (!replicableStore.serviceStatus()) {
                replicableStore.enable();
            }
            replicableStore.term(term);
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} enable store fail",
                    topicPartitionGroup, localNode, e);
        }

        logger.info("Partition group {}/node {} enable store elapse {} ms",
                topicPartitionGroup, localNode, SystemClock.now() - startTime);
    }

    /**
     * 转移leader到另外一个节点
     * @param transferee 转移目标节点id
     */
    private synchronized void transferLeadership(int transferee) throws ElectionException {
        if (!isLeader()) {
            return;
        }
        if (transferee == localNodeId) {
            logger.warn("Partition group {} transfer leader to self {}",
                    topicPartitionGroup, localNode);
            return;
        }
        if (!allNodes.containsKey(transferee)) {
            logger.warn("Partition group {} transfer leader transferee {} is incorrect node",
                    topicPartitionGroup, transferee);
            throw new ElectionException("Transfer leader to incorrection node");
        }
        if (transferee == INVALID_NODE_ID) {
            transferee = replicaGroup.findTheNextCandidate(leaderId);
            if (transferee == -1) {
                logger.warn("Partition group {}/node {} transfer leader, cannot find candidate",
                        topicPartitionGroup, localNode);
                throw new ElectionException("Transfer leader cannot find candidate");
            }
        }
        if (this.transferee != INVALID_NODE_ID) {
            logger.info("Partition group {} transfer leader, anoter transfer still not finish",
                    topicPartitionGroup);
            throw new ElectionException("Transfer leader, another transfer still in process");
        }

        logger.info("Partition group {}/node {} start transfer leader to {}",
                topicPartitionGroup, localNode, transferee);
        nodeOffline(currentTerm);

        replicaGroup.transferLeadershipTo(transferee, getLastLogPosition());
        this.transferee = transferee;
        transitionTo(TRANSFERRING);

        transferLeaderTimerFuture = electionTimerExecutor.schedule(new TransferLeaderTimerCallback(
                currentTerm), electionConfig.getTransferLeaderTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * 停止传输的参数，后续恢复时需要用到
     */
    private class TransferLeaderTimerCallback implements Runnable {
        private int term;

        TransferLeaderTimerCallback(int term) {
            this.term = term;
        }

        public int term() {
            return term;
        }

        @Override
        public void run () {
            stopTransferLeadership(term);
        }

    }

    /**
     * 停止转移leader定时器
     */
    private synchronized void cancelTransferLeaderTimer() {
        if (transferLeaderTimerFuture != null && !transferLeaderTimerFuture.isDone()) {
            transferLeaderTimerFuture.cancel(true);
            transferLeaderTimerFuture = null;
        }
    }

    /**
     * 停止转移leader，可能因为长时间没有成功，或者其他错
     */
    private synchronized void stopTransferLeadership(int term) {
        logger.info("Partition group {}/node {} transfer leadership time out, term is {}, current term is {}",
                topicPartitionGroup, localNode, term, currentTerm);
        cancelTransferLeaderTimer();
        if (term == currentTerm) {
            replicaGroup.stopTransferLeadership();
            if (state() == TRANSFERRING) {
                transitionTo(LEADER);
                nodeOnline(currentTerm);
				transferee = INVALID_NODE_ID;
            }
        }
    }

    /**
     * 处理TimeoutNow请求，该请求会引起当前节点立即选举
     * @param request 立即选举请求
     * @return 响应命令
     */
    public synchronized Command handleTimeoutNowRequest(TimeoutNowRequest request) {
        TimeoutNowResponse response = new TimeoutNowResponse(true, currentTerm);

        //noinspection ConstantConditions
        do {
            if (request.getTerm() != currentTerm) {
                logger.info("Partition group {}/node {} receive timeout now request, current term {}" +
                            " and request term {} not match",
                        topicPartitionGroup, localNode, currentTerm, request.getTerm());
                if (request.getTerm() > currentTerm) {
                    stepDown(request.getTerm());
                }
                response.setSuccess(false);
                break;
            }
            if (state() != FOLLOWER) {
                logger.info("Partition group {}/node {} receive timeout now request",
                        topicPartitionGroup, localNode);
                response.setSuccess(false);
                break;
            }

            response.setTerm(currentTerm + 1);

            resetElectionTimer();
            electSelf();

        } while(false);

        JoyQueueHeader header = new JoyQueueHeader(Direction.RESPONSE, CommandType.RAFT_TIMEOUT_NOW_RESPONSE);
        return new Command(header, response);
    }

    /**
     * Report leadership to name server
     */
    private void reportLeadership() {
        if (!isStarted()) {
            logger.info("Partition group {}/node {} election is close when report leader",
                    topicPartitionGroup, localNode);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Partition group {}/node {} enable report leader periodically is {}, state is {}",
                    topicPartitionGroup, localNode,
                    electionConfig.enableReportLeaderPeriodically(), state());
        }

        if (state() == LEADER) {
            if (electionConfig.enableReportLeaderPeriodically()) {
                if (electionConfig.enableReportLeaderPeriodicallyForce()) {
                    updateMetadata(localNodeId, currentTerm);
                } else {
                    TopicConfig topicConfig = clusterManager.getNameService().getTopicConfig(TopicName.parse(topicPartitionGroup.getTopic()));
                    if (topicConfig != null) {
                        PartitionGroup partitionGroup = topicConfig.fetchPartitionGroupByGroup(topicPartitionGroup.getPartitionGroupId());
                        if (partitionGroup != null && partitionGroup.getTerm() != null &&
                                (partitionGroup.getTerm() == null || !partitionGroup.getTerm().equals(currentTerm)
                                        || partitionGroup.getLeader() == null || !partitionGroup.getLeader().equals(localNodeId))) {
                            updateMetadata(localNodeId, currentTerm);
                        }
                    }
                }
            }

            if (electionConfig.enableOnlineNodePeriodically()) {
                maybeNodeOnline();
            }
        }
    }


    private int getRecommendLeader() {
        TopicConfig topicConfig = clusterManager.getNameService().getTopicConfig(
                TopicName.parse(topicPartitionGroup.getTopic()));
        PartitionGroup pg = topicConfig.getPartitionGroups().get(topicPartitionGroup.getPartitionGroupId());
        if (pg == null || pg.getRecLeader() == null) {
            return INVALID_NODE_ID;
        }
        return pg.getRecLeader();
    }

    private boolean shouldRebalanceLeader(int recommendLeader) {
        if (!electionConfig.enableRebalanceLeader()) {
            return false;
        }
        if (!isLeader()) {
            return false;
        }
        if (localNodeId == recommendLeader || recommendLeader == INVALID_NODE_ID ||
                !allNodes.containsKey(recommendLeader)) {
            return false;
        }

        long recommendLeaderLag = replicaGroup.lagLength(recommendLeader);
        if (recommendLeaderLag == ElectionNode.INVALID_LAG_LENGTH ||
                replicaGroup.lagLength(recommendLeader) > electionConfig.getTransferLeaderMinLag()) {
            return false;
        }

        if (SystemClock.now() - lastRebalanceTime < electionConfig.getMinRebalanceLeaderInterval()) {
            return false;
        }

        return true;
    }

    /**
     * Rebalance leader if needed
     */
    private void rebalanceLeader() {
        if (!isStarted()) {
            logger.info("Partition group {}/node {} election is close when rebalance leader",
                    topicPartitionGroup, localNode);
            return;
        }

        try {
            int recommendLeader = getRecommendLeader();

            if (recommendLeader == INVALID_NODE_ID) {
                logger.debug("Partition group {}/node {} rebalance leader, recommend leader is -1",
                        topicPartitionGroup, localNode);
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Partition group {}/node {} rebalance leader, recommend leader is {}, lag length is {} " +
                                "last rebalance time is {}, enable is {}",
                        topicPartitionGroup, localNode, recommendLeader,
                        replicaGroup.lagLength(recommendLeader), lastRebalanceTime,
                        electionConfig.enableRebalanceLeader());
            }

            if (shouldRebalanceLeader(recommendLeader)) {
                logger.info("Partition group {}/node {} transfer leadership to {}",
                        topicPartitionGroup, localNode, recommendLeader);

                transferLeadership(recommendLeader);

                lastRebalanceTime = SystemClock.now();
            }
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} rebalance leader fail",
                    topicPartitionGroup, localNode, e);
        }
    }

}
