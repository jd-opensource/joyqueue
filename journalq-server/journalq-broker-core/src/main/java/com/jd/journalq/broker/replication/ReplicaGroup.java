/**
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
package com.jd.journalq.broker.replication;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.election.DefaultElectionNode;
import com.jd.journalq.broker.election.ElectionConfig;
import com.jd.journalq.broker.election.ElectionNode;
import com.jd.journalq.broker.election.LeaderElection;
import com.jd.journalq.broker.election.TopicPartitionGroup;
import com.jd.journalq.broker.election.command.AppendEntriesRequest;
import com.jd.journalq.broker.election.command.AppendEntriesResponse;
import com.jd.journalq.broker.election.command.ReplicateConsumePosRequest;
import com.jd.journalq.broker.election.command.ReplicateConsumePosResponse;
import com.jd.journalq.broker.election.command.TimeoutNowRequest;
import com.jd.journalq.broker.election.command.TimeoutNowResponse;
import com.jd.journalq.broker.monitor.BrokerMonitor;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.store.replication.ReplicableStore;
import com.google.common.base.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import com.jd.journalq.toolkit.validate.annotation.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/26
 */
public class ReplicaGroup extends Service {
    private static Logger logger = LoggerFactory.getLogger(ReplicaGroup.class);

    private ElectionConfig electionConfig;
    private TopicPartitionGroup topicPartitionGroup;
    private ReplicationManager replicationManager;

    private List<Replica> replicas;
    private List<Replica> replicasWithoutLearners;

    private volatile ElectionNode.State state;

    private int localReplicaId;
    private int leaderId;
    private int currentTerm;

    private int transferee = ElectionNode.INVALID_NODE_ID;
    private long timeoutNowPosition = 0;

    private ReplicableStore replicableStore;

    private Thread replicateThread;
    private DelayQueue<DelayedCommand> replicateResponseQueue;

	private LeaderElection leaderElection;
    private ExecutorService replicateExecutor;
    private ScheduledExecutorService replicateTimerExecutor;

    private Consume consume;
    private BrokerMonitor brokerMonitor;

    private long lastLogTime = 0; //TODO 临时

    private static final long ONE_SECOND_NANO = 1000 * 1000 * 1000;
    private static final long ONE_MS_NANO     = 1000 * 1000;

    ReplicaGroup(TopicPartitionGroup topicPartitionGroup, ReplicationManager replicationManager,
                 ReplicableStore replicableStore, ElectionConfig electionConfig,
                 Consume consume, ExecutorService replicateExecutor,
                 ScheduledExecutorService replicateTimerExecutor, BrokerMonitor brokerMonitor,
                 List<DefaultElectionNode> allNodes, Set<Integer> learners, int localReplicaId, int leaderId
                        ) {
        Preconditions.checkArgument(electionConfig != null, "election config is null");
        Preconditions.checkArgument(topicPartitionGroup != null, "topic partition group is null");
        Preconditions.checkArgument(replicationManager != null, "replication manager is null");
        Preconditions.checkArgument(consume != null,  "consume is null");
        Preconditions.checkArgument(brokerMonitor != null, "broker monitor is null");
        Preconditions.checkArgument(replicateExecutor != null, "replicate executor is null");
        Preconditions.checkArgument(replicableStore != null, "replicable store is null");

        this.electionConfig = electionConfig;
        this.topicPartitionGroup = topicPartitionGroup;
        this.replicationManager = replicationManager;
        this.localReplicaId = localReplicaId;
        this.leaderId = leaderId;
        this.consume = consume;
        this.brokerMonitor = brokerMonitor;
        this.replicateExecutor = replicateExecutor;
        this.replicateTimerExecutor = replicateTimerExecutor;
        this.replicableStore = replicableStore;

        replicas = allNodes.stream()
                .map(n -> new Replica(n.getNodeId(), n.getAddress()))
                .collect(Collectors.toList());
        replicasWithoutLearners = replicas.stream()
                .filter(r -> !learners.contains(r.replicaId()))
                .collect(Collectors.toList());
    }


    @Override
    public void doStart() throws Exception {
        super.doStart();

        replicateResponseQueue = new DelayQueue<>();

        replicateThread = new ReplicateThread("ReplicateThread-" + localReplicaId);
        replicateThread.start();
    }

    @Override
    public void doStop() {
        replicateThread.interrupt();
        replicasWithoutLearners.forEach(this::cancelHeartbeatTimer);
        super.doStop();
    }

    public void setLeaderElection(LeaderElection leaderElection) {
        this.leaderElection = leaderElection;
    }

    /**
     * 添加节点
     * @param node 要添加的节点
     */
    public void addNode(ElectionNode node) {
        Replica newReplica = new Replica(node.getNodeId(), node.getAddress());
        newReplica.nextPosition(replicableStore.rightPosition());
        replicas.add(newReplica);

        replicateResponseQueue.put(new DelayedCommand(
                System.nanoTime() + ONE_SECOND_NANO, newReplica.replicaId()));

        startNewHeartbeat(newReplica);

        for (Replica replica : replicas) {
            logger.info("Partition group {}/node {} add node, replica {}'s next position is {}",
                    topicPartitionGroup, localReplicaId, replica.replicaId(), replica.nextPosition());
        }
    }

    /**
     * 删除节点
     * @param nodeId 要删除的节点Id
     */
    public void removeNode(int nodeId) {
        cancelHeartbeatTimer(getReplica(nodeId));

        replicas = replicas.stream()
                .filter(r -> r.replicaId() != nodeId)
                .collect(Collectors.toList());
        replicasWithoutLearners = replicasWithoutLearners.stream()
                .filter(r -> r.replicaId() != nodeId)
                .collect(Collectors.toList());
    }

    /**
     * 获取副本
     * @param replicaId 副本id
     * @return replica
     */
    private Replica getReplica(int replicaId) {
        return replicas.stream()
                .filter(r -> r.replicaId() == replicaId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 设置当前节点状态
     * @param state 节点状态
     */
    public void setState(ElectionNode.State state) {
        this.state = state;
    }

    /**
     * 是否需要复制，kafka的coordinators不需要复制
     * @return if topic need replicate
     */
    private boolean neednotReplicate() {
        return topicPartitionGroup.getTopic().equalsIgnoreCase("__group_coordinators");
    }

    /**
     * Get lag length of the replica to leader
     * @param replicaId replica id
     * @return lag length
     */
    public long lagLength(int replicaId) {
        return replicableStore.rightPosition() - getReplica(replicaId).writePosition();
    }

    /**
     * Set current replica as leader
     * - Init next position for each replica
     * - Send append entries request to all replicas
     * @param term 任期
     */
    public void becomeLeader(int term, int leaderId) {
        currentTerm = term;
        this.leaderId = leaderId;

        long writePosition = replicableStore.rightPosition();
        replicas.forEach(r -> {
            r.nextPosition(writePosition);
            r.setMatch(false);
        });

        state = ElectionNode.State.LEADER;

        replicasWithoutLearners.forEach(this::startNewHeartbeat);

        logger.info("Partition group {}/node {} become leader, term is {}, writePosition is {}, " +
                    "commit position is {}",
                topicPartitionGroup, leaderId, term, writePosition, replicableStore.commitPosition());

    }

    /**
     * Set current node as follower
     * @param term 任期
     * @param leaderId leader id
     */
    public void becomeFollower(int term, int leaderId) {
        logger.info("Partition group {}/node {} become follower, term is {}, leader is {}, " +
                    "write position is {}, commit position is {}",
                topicPartitionGroup, localReplicaId, term, leaderId, replicableStore.rightPosition(),
                replicableStore.commitPosition());
        state = ElectionNode.State.FOLLOWER;
        currentTerm = term;
        this.leaderId = leaderId;

        replicasWithoutLearners.forEach(this::cancelHeartbeatTimer);

    }

    /**
     * 复制消息的线程
     * 1. 通过一个阻塞队列保证收到副本的复制消息响应继续复制下一批消息
     * 2. 当阻塞队列中有数据时，给该副本发送复制消息
     * 3. 每隔一定时间复制消费位置
     */
    class ReplicateThread extends Thread {
        private ReplicateThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            initResponseQueue();

            while (true) {
                try {
                    if (!ReplicaGroup.this.isStarted() || (state != ElectionNode.State.LEADER && state != ElectionNode.State.TRANSFERRING)) {
                        Thread.sleep(100);
                        continue;
                    }

                    if (neednotReplicate()) return;

                    DelayedCommand command = replicateResponseQueue.take();
                    if (command.replicaId() == localReplicaId) {
                        replicateLocal();
                        continue;
                    }

                    if (!replicas.contains(getReplica(command.replicaId()))) {
                        logger.info("Partition group {}/node {} not contain this node",
                                topicPartitionGroup, localReplicaId);
                        continue;
                    }

                    replicateMessage(getReplica(command.replicaId()));
                    maybeReplicateConsumePos(getReplica(command.replicaId()));

                } catch (InterruptedException ie) {
                    logger.info("Partition group {}/node {} replicate interrupted",
                            topicPartitionGroup, localReplicaId, ie);
                    break;
                } catch (Throwable t) {
                    logger.warn("Partition group {}/node {} replicate fail",
                            topicPartitionGroup, localReplicaId, t);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

    }

    /**
     * 初始化响应阻塞队列，启动向副本复制消息
     */
    private void initResponseQueue() {
        replicas.forEach((r) -> replicateResponseQueue.put(
                new DelayedCommand(System.nanoTime(), r.replicaId())));
    }

    /**
     * 如果只有一个节点，直接commit
     */
    private void replicateLocal() {
        if (replicas.size() == 1) {
            if (replicableStore.commitPosition() < replicableStore.rightPosition()) {
                replicableStore.commit(replicableStore.rightPosition());
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
            replicateResponseQueue.put(new DelayedCommand(System.nanoTime(), localReplicaId));
        }

    }

    /**
     * Replicate logs to a replica
     * @param replica 副本
     */
    private void replicateMessage(Replica replica) {
        try {
            replicateExecutor.submit(() -> {
                try {
                    long startTimeUs = usTime();

                    AppendEntriesRequest request = generateAppendEntriesRequest(replica);
                    if (request == null) {
                        replicateResponseQueue.put(new DelayedCommand(
                                System.nanoTime() + ONE_MS_NANO, replica.replicaId()));
                        return;
                    }

                    JournalqHeader header = new JournalqHeader(Direction.REQUEST, CommandType.RAFT_APPEND_ENTRIES_REQUEST);

                    if (SystemClock.now() - lastLogTime > electionConfig.getLogInterval()) {
                        logger.info("Partition group {}/node {} send append entries request {} to node {}, " +
                                "read entries elapse {} us",
                                topicPartitionGroup, leaderId, request, replica.replicaId(), usTime() - startTimeUs);
                    }

                    replicationManager.sendCommand(replica.getAddress(), new Command(header, request),
                            electionConfig.getSendCommandTimeout(),
                            new AppendEntriesRequestCallback(replica, startTimeUs, request.getEntriesLength()));

                } catch (Throwable t) {
                    logger.warn("Partition group {}/ node {} send append entries to {} fail",
                            topicPartitionGroup, localReplicaId, replica.replicaId(), t);
                    replicateResponseQueue.put(new DelayedCommand(System.nanoTime() + ONE_SECOND_NANO, replica.replicaId()));
                }
            });
        } catch (Exception e) {
            logger.info("Partition group {}/node {} replicate message to {} fail",
                    topicPartitionGroup, localReplicaId, replica.replicaId(), e);
            replicateResponseQueue.put(new DelayedCommand(
                    System.nanoTime() + ONE_SECOND_NANO, replica.replicaId()));
        }
    }

    /**
     * 构造复制消息请求
     * @param replica 副本
     * @return 复制消息请求
     * @throws Exception 异常
     */
    private AppendEntriesRequest generateAppendEntriesRequest(Replica replica) throws Exception {

        long leftPosition = replicableStore.leftPosition();
        long startPosition = Math.max(replica.nextPosition(), leftPosition);

        if (startPosition >= replicableStore.rightPosition()) return null;

        ByteBuffer entries;
        try {
            entries = replicableStore.readEntryBuffer(startPosition, electionConfig.getMaxReplicateLength());
        } catch (Exception e) {
            logger.info("Partition group {}/node {} read entries from {} fail rollback to prev",
                    topicPartitionGroup, localReplicaId, startPosition, e);
            long oldPosition = startPosition;
            startPosition = getPrevPosition(startPosition);
            replica.nextPosition(startPosition);
            logger.info("Partition group {}/node {} get prev position of {} return {}, left position is {}",
                    topicPartitionGroup, localReplicaId, oldPosition, startPosition, leftPosition);
            entries = replicableStore.readEntryBuffer(startPosition, electionConfig.getMaxReplicateLength());
        }
        if (entries == null || !entries.hasRemaining()) return null;

        int entriesTerm = replicableStore.getEntryTerm(startPosition);

        long prevPosition = 0;
        int prevTerm = 0;
        if (!replica.isMatch() && startPosition > leftPosition) {
            prevPosition = replicableStore.position(startPosition, -1);
            logger.info("Partition group {}/node {} generate append entries request, " +
                            "start position is {}, prev pos is {}, left pos is {}",
                    topicPartitionGroup, localReplicaId, startPosition, prevPosition,leftPosition);
            prevTerm = replicableStore.getEntryTerm(prevPosition);
        }
        
        return AppendEntriesRequest.Build.create().partitionGroup(topicPartitionGroup)
                .leader(leaderId).term(currentTerm).startPosition(startPosition)
                .leftPosition(leftPosition).match(replica.isMatch())
                .commitPosition(replicableStore.commitPosition()).prevTerm(prevTerm)
                .prevPosition(prevPosition).entriesTerm(entriesTerm).entries(entries)
                .build();
    }

    /**
     * Callback of replicate logs request
     */
    private class AppendEntriesRequestCallback implements CommandCallback {
        private Replica replica;
        private long startTimeUs;
        private int entriesLength;

        AppendEntriesRequestCallback(Replica replica, long startTimeUs, int entriesLength) {
            this.replica = replica;
            this.startTimeUs = startTimeUs;
            this.entriesLength = entriesLength;
        }

        @Override
        public void onSuccess(Command request, Command response) {
            try {
                AppendEntriesRequest appendEntriesRequest = (AppendEntriesRequest)request.getPayload();
                AppendEntriesResponse appendEntriesResponse = (AppendEntriesResponse)response.getPayload();

                if (SystemClock.now() - lastLogTime > electionConfig.getLogInterval()) {
                    logger.info("Partition group {}/node {} receive append entries response from {}, " +
                                    "success is {}, next position is {}, write position is {}, elapse {} us",
                            topicPartitionGroup, localReplicaId, replica.replicaId(), appendEntriesResponse.isSuccess(),
                            appendEntriesResponse.getNextPosition(), appendEntriesResponse.getWritePosition(),
                            usTime() - startTimeUs);
                }

                if (appendEntriesRequest.getTerm() != currentTerm) {
                    logger.info("Partition group {}/node {} append entries request term {} not equals current term {}",
                            topicPartitionGroup, localReplicaId, appendEntriesRequest.getTerm(), currentTerm);
                    return;
                }
                if (appendEntriesResponse.getTerm() > currentTerm) {
                    logger.info("Partition group {}/node {} append entries response term {} not equals current term {}",
                            topicPartitionGroup, localReplicaId, appendEntriesResponse.getTerm(), currentTerm);
                    leaderElection.stepDown(appendEntriesResponse.getTerm());
                    return;
                }

                processAppendEntriesResponse(appendEntriesResponse, replica);

                brokerMonitor.onReplicateMessage(topicPartitionGroup.getTopic(), topicPartitionGroup.getPartitionGroupId(),
                        1, entriesLength, usTime() - startTimeUs);

            } catch (Exception e) {
                logger.info("Partition group {}/node {} process append entries reponse fail",
                        topicPartitionGroup, localReplicaId, e);
            } finally {
                replicateResponseQueue.put(new DelayedCommand(System.nanoTime(), replica.replicaId()));
            }
        }

        @Override
        public void onException(Command request, Throwable cause) {
            try {
                AppendEntriesRequest appendEntriesRequest = (AppendEntriesRequest) request.getPayload();
                logger.info("Partition group {}/node {} send append entries request to {} failed, position is {}, " +
                                "current term is {}",
                        topicPartitionGroup, localReplicaId, replica.replicaId(),
                        appendEntriesRequest.getStartPosition(), currentTerm, cause);

            } catch (Exception e) {
                logger.warn("Partition group {}/node {} send append entries onException fail, request is {}",
                        topicPartitionGroup, localReplicaId, request, e);
            } finally {
                replicateResponseQueue.put(
                        new DelayedCommand(System.nanoTime() + ONE_SECOND_NANO, replica.replicaId()));
            }
        }
    }

    /**
     * Process the response of append entries request
     * Update the commit position as the majority value of all replica's write position
     * @param response 写入记录响应
     */
    private synchronized void processAppendEntriesResponse(AppendEntriesResponse response, Replica replica) {
        replica.lastAppendSuccessTime(SystemClock.now());

        if (!response.isSuccess()) {
            if (response.getNextPosition() == -1L) replica.nextPosition(getPrevPosition(replica.nextPosition()));
            else replica.nextPosition(getPrevPosition(response.getNextPosition()));
            return;
        }

        replica.writePosition(response.getWritePosition());
        replica.nextPosition(response.getNextPosition());
        replica.setMatch(true);

        if (transferee != ElectionNode.INVALID_NODE_ID && replica.nextPosition() >= timeoutNowPosition) {
            sendTimeoutNowRequest(transferee);
        }

        getReplica(leaderId).writePosition(replicableStore.rightPosition());
        replicasWithoutLearners.sort((r1, r2) ->
                Long.valueOf(r1.writePosition()).compareTo(r2.writePosition()));

        long commitPosition = replicasWithoutLearners.get(replicasWithoutLearners.size() / 2).writePosition();
        replicableStore.commit(commitPosition);

        if (SystemClock.now() - lastLogTime > electionConfig.getLogInterval()) {
            replicasWithoutLearners.forEach(r -> logger.debug("Partition group {}/node {}", topicPartitionGroup, r));
            logger.info("Partition group {}/node {} commit position is {}",
                    topicPartitionGroup, localReplicaId, replicableStore.commitPosition());
            lastLogTime = SystemClock.now();
        }

    }

    /**
     * Replicate consume position to a replica
     * @param replica 副本
     */
    private void maybeReplicateConsumePos(Replica replica) {
        long now = SystemClock.now();
        if (now - replica.lastReplicateConsumePosTime() < electionConfig.getReplicateConsumePosInterval()) {
            return;
        }
        replica.lastReplicateConsumePosTime(now);

        try {
            String consumePositions = consume.getConsumeInfoByGroup(TopicName.parse(topicPartitionGroup.getTopic()),
                    null, topicPartitionGroup.getPartitionGroupId());
            if (consumePositions == null) {
                logger.info("Partition group {}/node {} get consumer info return null",
                        topicPartitionGroup, localReplicaId);
                return;
            }

            ReplicateConsumePosRequest request = new ReplicateConsumePosRequest(consumePositions);
            JournalqHeader header = new JournalqHeader(Direction.REQUEST, CommandType.REPLICATE_CONSUME_POS_REQUEST);

            logger.debug("Partition group {}/node {} send consume position {} to node {}",
                    topicPartitionGroup, localReplicaId, consumePositions, replica.replicaId());

            replicateExecutor.submit(() -> {
                try {
                    replicationManager.sendCommand(replica.getAddress(), new Command(header, request),
                            electionConfig.getSendCommandTimeout(), new ReplicateConsumePosRequestCallback(replica));
                } catch (Exception e) {
                    logger.warn("Partition group {}/node {} send replicate consume pos message fail",
                            topicPartitionGroup, localReplicaId, e);
                }
            });
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} replicate consume position task failed",
                    topicPartitionGroup, localReplicaId, e);
        }
    }

    /**
     * Callback of replicate consume pos request command
     */
    private class ReplicateConsumePosRequestCallback implements CommandCallback {
        private Replica replica;

        ReplicateConsumePosRequestCallback(Replica replica) {
            this.replica = replica;
        }

        @Override
        public void onSuccess(Command request, Command responseCommand) {
            ReplicateConsumePosResponse response = (ReplicateConsumePosResponse)responseCommand.getPayload();
            if (!response.isSuccess()) {
                logger.info("Partition group {}/node {} replicate consume pos to {} fail",
                        topicPartitionGroup, localReplicaId, replica.replicaId());
            }
        }

        @Override
        public void onException(Command request, Throwable cause) {
            logger.info("Partition group {}/node {} replicate consume pos to {} fail",
                    topicPartitionGroup, localReplicaId, replica.replicaId(), cause);
        }
    }

    /**
     * Append entries to store
     * @param request 添加记录请求
     * @return 返回命令
     */
    public Command appendEntries(AppendEntriesRequest request) {
        long startPosition = request.getStartPosition();
        long nextPosition = request.getStartPosition();
        int entriesLength = request.getEntries().remaining();
        boolean success = true;

        logger.debug("Partition group {}/node {} receive append entries request {}, start position " +
                    "is {}, write position is {}, commit position is {}",
                topicPartitionGroup, localReplicaId, request, startPosition, replicableStore.rightPosition(),
                replicableStore.commitPosition());
        //noinspection ConstantConditions
        do {
            try {
                if (state != ElectionNode.State.FOLLOWER) {
                    logger.info("Partition group {}/node {} receive append entries request {}, state is {}",
                            topicPartitionGroup, localReplicaId, request, state);
                    success = false;
                    break;
                }

                if (!matchPosition(request.getStartPosition(), request.getLeftPosition(), request.getPrevTerm(),
                        request.getPrevPosition(), request.isMatch())) {
                    if (request.getStartPosition() > replicableStore.rightPosition()) {
                        logger.info("Partition group {}/node {} match position, position is {}, " +
                                "write position is {}, left position is {}",
                                topicPartitionGroup, localReplicaId,
                                request.getStartPosition(), replicableStore.rightPosition(), request.getLeftPosition());
                        if (replicableStore.rightPosition() > request.getLeftPosition()) {
                            nextPosition = replicableStore.rightPosition();
                        } else {
                            nextPosition = request.getLeftPosition();
                        }
                    } else {
                        nextPosition = -1;
                    }
                    success = false;
                    break;
                }

                if (request.getStartPosition() != replicableStore.rightPosition()) {
                    logger.info("Partition group {}/node {} match position, position is {}, write position is {}, " +
                                    "left position is {}",
                            topicPartitionGroup, localReplicaId, request.getStartPosition(),
                            replicableStore.rightPosition(), request.getLeftPosition());
                    replicableStore.setRightPosition(request.getStartPosition());
                }

                long startTimeUs = usTime();
                nextPosition = replicableStore.appendEntryBuffer(request.getEntries());

                if (SystemClock.now() - lastLogTime > electionConfig.getLogInterval()) {
                    logger.info("Partition group {}/node {}, append entries from {}, position is {}, entry length is {}, " +
                                    "commit position is {}, elapse {} us",
                            topicPartitionGroup, localReplicaId, request.getLeaderId(), startPosition,
                            entriesLength, request.getCommitPosition(), usTime() - startTimeUs);
                    lastLogTime = SystemClock.now();
                }

                brokerMonitor.onAppendReplicateMessage(topicPartitionGroup.getTopic(), topicPartitionGroup.getPartitionGroupId(),
                        1, request.getEntriesLength(), usTime() - startTimeUs);

                replicableStore.commit(request.getCommitPosition());

            } catch (TimeoutException te) {
                logger.warn("Partition group {}/node {} append entries to position {} timeout, entries length is {}",
                        topicPartitionGroup, localReplicaId, startPosition, entriesLength, te);
                success = false;
                nextPosition = startPosition;

            } catch (Throwable t) {
                logger.warn("Partition group {}/node {} append entries to position {} failed, write position is {}， " +
                            "entries length is {}",
                        topicPartitionGroup, localReplicaId, startPosition, replicableStore.rightPosition(), entriesLength, t);
                success = false;
                nextPosition = -1L;
                break;
            }
        } while(false);

        AppendEntriesResponse response = AppendEntriesResponse.Build.create().topicPartitionGroup(topicPartitionGroup)
                .term(currentTerm).writePosition(replicableStore.rightPosition()).nextPosition(nextPosition)
                .replicaId(localReplicaId).success(success).entriesTerm(request.getEntriesTerm())
                .build();

        return new Command(new JournalqHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE), response);
    }

    /**
     * Match the log on leader and follower
     * @param startPosition  position of the log to be compare
     * @param leftPosition left position of the leader logs
     * @param prevTerm  previous log's term
     * @param prevPosition  previous log's start position
     * @return matched position
     */
    private boolean matchPosition(long startPosition, long leftPosition, int prevTerm, long prevPosition, boolean isMatch) {
        boolean match = false;
        int localPrevTerm = -1;

        if (startPosition == leftPosition) {
            logger.info("Partition group {}/node {} match position start position {} equals left position",
                    topicPartitionGroup, localReplicaId, startPosition);
            return true;
        }

        if (startPosition > replicableStore.rightPosition()) {
            logger.info("Partition group {}/node {} match position start position {} bigger then right position {}",
                    topicPartitionGroup, localReplicaId, startPosition, replicableStore.rightPosition());
            return false;
        }

        if (isMatch) return true;

        if (prevPosition > replicableStore.leftPosition()) {
            try {
                localPrevTerm = replicableStore.getEntryTerm(prevPosition);
            } catch (Exception e) {
                logger.info("Partition group {}/node {} match position get entry term fail, " +
                        "start position is {}, prev position is {}, left position is {}, right position is {}",
                        topicPartitionGroup, localReplicaId, startPosition, prevPosition, leftPosition,
                        replicableStore.rightPosition(), e);
            }
        }

        logger.info("Partition group {}/node {} match prev position and term, position is {}, left position is {}, " +
                        "prev position is {}, prev term is {},  local prev term is {}, right position is {}",
                topicPartitionGroup, localReplicaId, startPosition, leftPosition,
                prevPosition, prevTerm, localPrevTerm, replicableStore.rightPosition());


        // if term of local previous position not match the leader
        // should match previous message
        if (prevTerm == localPrevTerm) match = true;

        return match;

    }

    /**
     * 开始新一轮心跳，向Follower节点发送心跳命令，重置心跳定时器
     */
    private void startNewHeartbeat(Replica replica) {
        if (!isStarted()) {
            logger.info("Partition group {}/node {} start new heartbeat, replicator not start",
                    topicPartitionGroup, localReplicaId);
            return;
        }

        if (state != ElectionNode.State.LEADER && state != ElectionNode.State.TRANSFERRING) {
            logger.info("Partition group {}/node {} start new heartbeat, state is {}",
                    topicPartitionGroup, localReplicaId, state);
            return;
        }

        if (replica.replicaId() == localReplicaId) {
            logger.info("Partition group {}/node {} start new heartbeat, replica is local",
                    topicPartitionGroup, localReplicaId);
            return;
        }

        AppendEntriesRequest appendEntriesRequest = AppendEntriesRequest.Build.create()
                .partitionGroup(topicPartitionGroup).term(currentTerm).leader(leaderId).build();

        try {
            replicateExecutor.submit(() -> {
                JournalqHeader header = new JournalqHeader(Direction.REQUEST, CommandType.RAFT_APPEND_ENTRIES_REQUEST);
                Command command = new Command(header, appendEntriesRequest);

                logger.debug("Partition group {}/node{} send heartbeat request {} to {}",
                        topicPartitionGroup, localReplicaId, appendEntriesRequest, replica.replicaId());
                try {
                    replicationManager.sendCommand(replica.getAddress(), command,
                            electionConfig.getSendCommandTimeout(), new HeartbeatRequestCallback(replica));
                } catch (Exception e) {
                    logger.warn("Partition group {}/node{} send heartbeat to {} fail",
                            topicPartitionGroup, localReplicaId, replica.replicaId(), e);
                    resetHeartbeatTimer(replica);
                }
            });
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} submit new heartbeat task fail",
                    topicPartitionGroup, localReplicaId, e);
            resetHeartbeatTimer(replica);
        }

    }

    /**
     * Callback of send heartbeat request command
     */
    private class HeartbeatRequestCallback implements CommandCallback {
        private Replica replica;

        HeartbeatRequestCallback(Replica replica) {
            this.replica = replica;
        }

        @Override
        public void onSuccess(Command request, Command responseCommand) {
            handleHeartbeatResponse(responseCommand, replica);
            if (replicasWithoutLearners.contains(replica)) {
                resetHeartbeatTimer(replica);
            } else {
                logger.info("Partition group {}/node {} receive heartbeat response from unknown node {}",
                        topicPartitionGroup, localReplicaId, replica.replicaId());
            }
        }

        @Override
        public void onException(Command request, Throwable cause) {
            logger.info("Partition group {}/node {} send heartbeat request to {} failed",
                    topicPartitionGroup, localReplicaId, replica.replicaId(), cause);
            if (replicasWithoutLearners.contains(replica)) {
                resetHeartbeatTimer(replica);
            }
        }
    }


    /**
     * 重置心跳定时器
     */
    private void resetHeartbeatTimer(Replica replica) {
        synchronized (replica) {
            ScheduledFuture heartbeatTimerFuture = replica.heartbeatTimerFuture();
            if (heartbeatTimerFuture != null && !heartbeatTimerFuture.isDone()) {
                heartbeatTimerFuture.cancel(true);
            }
            heartbeatTimerFuture = replicateTimerExecutor.schedule(() -> startNewHeartbeat(replica),
                    electionConfig.getHeartbeatTimeout(), TimeUnit.MILLISECONDS);
            replica.heartbeatTimerFuture(heartbeatTimerFuture);
        }
    }

    /**
     * 取消心跳定时器
     */
    private void cancelHeartbeatTimer(Replica replica) {
        synchronized (replica) {
            ScheduledFuture heartbeatTimerFuture = replica.heartbeatTimerFuture();
            if (heartbeatTimerFuture != null && !heartbeatTimerFuture.isDone()) {
                heartbeatTimerFuture.cancel(true);
                replica.heartbeatTimerFuture(null);
            }
        }
    }

    /**
     * 处理心跳响应消息
     * @param command 心跳响应命令
     */
    private void handleHeartbeatResponse(Command command, Replica replica) {
        if (command == null) {
            logger.warn("Partition group {}/node{} receive heartbeat response is null",
                    topicPartitionGroup, localReplicaId);
            return;
        }

        AppendEntriesResponse response = (AppendEntriesResponse)command.getPayload();

        replica.lastAppendSuccessTime(SystemClock.now());

        logger.debug("Partition group {}/node{} receive heartbeat response from {}, term is {}",
                topicPartitionGroup, localReplicaId, replica.replicaId(), response.getTerm());

        if (response.getTerm() > currentTerm) {
            logger.info("Partition group{}/node{} receive heartbeat response, " +
                            "response term {} is greater than current term {}",
                    topicPartitionGroup, localReplicaId, response.getTerm(), currentTerm);
            leaderElection.stepDown(response.getTerm());
        }
    }


    /**
     * 获取上一条消息的位置，如果有异常则返回最左边位置
     * @param position 当前位置
     * @return 上一条消息位置
     */
    private long getPrevPosition(long position) {
        try {
            return replicableStore.position(position, -1);
        } catch (Throwable t) {
            long leftPosition = replicableStore.leftPosition();
            logger.warn("Partition group {}/node {} get previous position " +
                        "of position {} fail, return left position {}",
                    topicPartitionGroup, localReplicaId, position, leftPosition);
            return leftPosition;
        }
    }

    /**
     * Find the next candidate, followers with max position will be candidate
     * This is used by leadership transfer
     * @param leaderId leader id
     * @return candidate id
     */
    public int findTheNextCandidate(int leaderId) {
        long maxPosition = -1;
        int candidateId = -1;
        for(Replica replica : replicas) {
            if (replica.replicaId() != leaderId && replica.nextPosition() > maxPosition) {
                maxPosition = replica.nextPosition();
                candidateId = replica.replicaId();
            }
        }
        return candidateId;
    }

    /**
     * Transfer leadership to transferee
     * Timeout now request will be send to transferee when transferee catch up to leader
     * @param transferee transferee
     * @param logPosition max log position of leader
     * @throws TransportException transport exception
     */
    public void transferLeadershipTo(int transferee, long logPosition) throws TransportException {
        this.transferee = transferee;

        logger.info("Partition group {}/node {} transfer leadership to {}, log position is {}, " +
                    "transferee next position is {}",
                topicPartitionGroup, localReplicaId, transferee, logPosition,
                getReplica(transferee).nextPosition());

        if (getReplica(transferee).nextPosition() >= logPosition) {
            sendTimeoutNowRequest(transferee);
        }

        timeoutNowPosition = logPosition;
    }

    public void stopTransferLeadership() {
        this.transferee = -1;
        timeoutNowPosition = 0;
    }

    /**
     * Callback of send timeout now request command
     */
    private class TimeoutNowRequestCallback implements CommandCallback {
        @Override
        public void onSuccess(Command request, Command responseCommand) {
            TimeoutNowResponse response = (TimeoutNowResponse)responseCommand.getPayload();
            logger.info("Partition group {}/node {} timeout now request receive response, success is {}, " +
                    "response term is {}",
                    topicPartitionGroup, localReplicaId, response.isSuccess(), response.getTerm());
            
			if (response.getTerm() > currentTerm) {
				leaderElection.stepDown(response.getTerm());
			}
			
            transferee = -1;
            timeoutNowPosition = 0;
        }

        @Override
        public void onException(Command request, Throwable cause) {
            logger.info("Partition group {}/node {} timeout now request fail",
                    topicPartitionGroup, localReplicaId, cause);
            transferee = -1;
            timeoutNowPosition = 0;
        }
    }

    private void sendTimeoutNowRequest(int transferee) throws TransportException {
        logger.info("Partition group {}/node {} send timeout now request to {}",
                topicPartitionGroup, localReplicaId, transferee);

        TimeoutNowRequest request = new TimeoutNowRequest(topicPartitionGroup, currentTerm);
        JournalqHeader header = new JournalqHeader(Direction.REQUEST, CommandType.RAFT_TIMEOUT_NOW_REQUEST);

        replicationManager.sendCommand(getReplica(transferee).getAddress(), new Command(header, request),
                electionConfig.getSendCommandTimeout(), new TimeoutNowRequestCallback());
    }


    private class DelayedCommand implements Delayed {
        private long delayTimeNs;
        private int replicaId;

        DelayedCommand(long delayTimeNs, int replicaId) {
            this.delayTimeNs = delayTimeNs;
            this.replicaId = replicaId;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(delayTimeNs - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed another) {
            return Long.compare(delayTimeNs, ((DelayedCommand)another).delayTimeNs);
        }

        int replicaId() {
            return replicaId;
        }
    }

    private long usTime() {
        return System.nanoTime() / 1000;
    }
}
