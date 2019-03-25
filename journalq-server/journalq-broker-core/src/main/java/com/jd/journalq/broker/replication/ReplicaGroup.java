package com.jd.journalq.broker.replication;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.election.DefaultElectionNode;
import com.jd.journalq.broker.election.ElectionConfig;
import com.jd.journalq.broker.election.ElectionNode;
import com.jd.journalq.broker.election.TopicPartitionGroup;
import com.jd.journalq.broker.election.command.*;
import com.jd.journalq.broker.monitor.BrokerMonitor;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.store.replication.ReplicableStore;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import com.jd.ump.profiler.CallerInfo;
import com.jd.ump.profiler.proxy.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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

    private ElectionNode.State state;

    private int localReplicaId;
    private int leaderId;
    private int currentTerm;
    private long commitPosition;

    private int transferee = ElectionNode.INVALID_NODE_ID;
    private long timeoutNowPosition = 0;

    private ReplicableStore replicableStore;

    private ExecutorService replicateExecutor;
    private Thread replicateThread;
    private DelayQueue<DelayedCommand> replicateResponseQueue;

    private Consume consume;
    private BrokerMonitor brokerMonitor;

    private long lastLogTime = 0; //TODO 临时

    private static final long ONE_SECOND_NANO = 1000 * 1000 * 1000;
    private static final long ONE_MS_NANO     = 1000 * 1000;

    ReplicaGroup(TopicPartitionGroup topicPartitionGroup, ReplicationManager replicationManager,
                 ReplicableStore replicableStore, ElectionConfig electionConfig,
                 Consume consume, ExecutorService replicateExecutor, BrokerMonitor brokerMonitor,
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

        commitPosition = replicableStore.commitPosition();

    }

    @Override
    public void doStop() {
        replicateThread.interrupt();
        super.doStop();
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
                .orElseThrow(NoSuchElementException::new);
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
     * Set current replica as leader
     * - Init next position for each replica
     * - Send append entries request to all replicas
     * @param term 任期
     */
    public void becomeLeader(int term, int leaderId) {
        state = ElectionNode.State.LEADER;
        currentTerm = term;
        this.leaderId = leaderId;

        long writePosition = replicableStore.rightPosition();
        replicas.forEach(r -> {
            r.nextPosition(writePosition);
            r.setMatch(false);
        });

        logger.info("Partition group {}/node {} become leader, term is {}, writePosition is {}, " +
                    "commit position is {}",
                topicPartitionGroup, leaderId, term, writePosition, commitPosition);

    }

    /**
     * Set current node as follower
     * @param term 任期
     * @param leaderId leader id
     */
    public void becomeFollower(int term, int leaderId) {
        logger.info("Partition group {}/node {} become follower, term is {}, leader is {}, " +
                    "write position is {}, commit position is {}",
                topicPartitionGroup, localReplicaId, term, leaderId, replicableStore.rightPosition(), commitPosition);
        state = ElectionNode.State.FOLLOWER;
        currentTerm = term;
        this.leaderId = leaderId;

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
        replicas.forEach((r) -> replicateResponseQueue.put(new DelayedCommand(System.nanoTime(), r.replicaId())));
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
        replica.lastReplicateMessageTime(System.currentTimeMillis());

        try {

            replicateExecutor.submit(() -> {
                Thread.currentThread().setName("SendReplicateMessage-" + Thread.currentThread().getId());

                try {
                    long startTime = System.currentTimeMillis();

                    AppendEntriesRequest request = generateAppendEntriesRequest(replica);
                    if (request == null) {
                        replicateResponseQueue.put(new DelayedCommand(
                                System.nanoTime() + ONE_MS_NANO, replica.replicaId()));
                        return;
                    }

                    JMQHeader header = new JMQHeader(Direction.REQUEST, CommandType.RAFT_APPEND_ENTRIES_REQUEST);

                    if (startTime - lastLogTime > electionConfig.getLogInterval()) {
                        logger.info("Partition group {}/node {} send append entries request {} to node {}",
                                topicPartitionGroup, leaderId, request, replica.replicaId());
                    }

                    replicationManager.sendCommand(replica.getAddress(), new Command(header, request),
                            electionConfig.getSendCommandTimeout(),
                            new AppendEntriesRequestCallback(replica, startTime));

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

        long prevPosition = 0;
        int prevTerm = 0;
        if (!replica.isMatch() && startPosition > leftPosition) {
            prevPosition = replicableStore.position(startPosition, -1);
            prevTerm = replicableStore.getEntryTerm(prevPosition);
        }

        ByteBuffer entries = replicableStore.readEntryBuffer(startPosition, electionConfig.getMaxReplicateLength());
        if (entries == null || !entries.hasRemaining()) return null;

        return AppendEntriesRequest.Build.create().partitionGroup(topicPartitionGroup)
                .leader(leaderId).term(currentTerm).startPosition(startPosition)
                .leftPosition(leftPosition).match(replica.isMatch())
                .commitPosition(commitPosition).prevTerm(prevTerm)
                .prevPosition(prevPosition).entries(entries).build();
    }

    /**
     * Replicate consume position to a replica
     * @param replica 副本
     */
    private void maybeReplicateConsumePos(Replica replica) {
        long now = System.currentTimeMillis();
        if (now - replica.lastReplicateConsumePosTime() < electionConfig.getReplicateConsumePosInterval()) {
            return;
        }
        replica.lastReplicateConsumePosTime(now);

        try {
            String consumePositions = consume.getConsumeInfoByGroup(TopicName.parse(topicPartitionGroup.getTopic()),
                    null, topicPartitionGroup.getPartitionGroupId());

            ReplicateConsumePosRequest request = new ReplicateConsumePosRequest(consumePositions);
            JMQHeader header = new JMQHeader(Direction.REQUEST, CommandType.REPLICATE_CONSUME_POS_REQUEST);

            logger.debug("Partition group {}/node {} send consume position {} to node {}",
                    topicPartitionGroup, localReplicaId, consumePositions, replica.replicaId());

            replicateExecutor.submit(() -> {
                Thread.currentThread().setName("SendReplicateConsumeMessage-" + Thread.currentThread().getId());
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
     * Callback of replicate logs request
     */
    private class AppendEntriesRequestCallback implements CommandCallback {
        private Replica replica;
        private long startTime;

        AppendEntriesRequestCallback(Replica replica, long startTime) {
            this.replica = replica;
            this.startTime = startTime;
        }

        @Override
        public void onSuccess(Command request, Command response) {
            try {
                AppendEntriesRequest appendEntriesRequest = (AppendEntriesRequest)request.getPayload();
                AppendEntriesResponse appendEntriesResponse = (AppendEntriesResponse)response.getPayload();

                if (System.currentTimeMillis() - lastLogTime > electionConfig.getLogInterval()) {
                    logger.info("Partition group {}/node {} receive append entries response from {}, " +
                                    "success is {}, next position is {}, write position is {}, elapse {}",
                            topicPartitionGroup, localReplicaId, replica.replicaId(), appendEntriesResponse.isSuccess(),
                            appendEntriesResponse.getNextPosition(), appendEntriesResponse.getWritePosition(),
                            System.currentTimeMillis() - startTime);
                }

                if (appendEntriesRequest.getTerm() != currentTerm) {
                    logger.warn("Partition group {}/node {} append entries request term {} not equals current term {}",
                            topicPartitionGroup, localReplicaId, appendEntriesRequest.getTerm(), currentTerm);
                    return;
                }

                processAppendEntriesResponse(appendEntriesResponse, replica);

                brokerMonitor.onReplicateMessage(topicPartitionGroup.getTopic(), topicPartitionGroup.getPartitionGroupId(),
                        1, appendEntriesRequest.getEntriesLength(), System.currentTimeMillis() - startTime);

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
                if (appendEntriesRequest.getTerm() != currentTerm) {
                    logger.info("Partition group {}/node {} append entries request term {} not equals current term {}",
                            topicPartitionGroup, localReplicaId, appendEntriesRequest.getTerm(), currentTerm);
                }

                logger.info("Partition group {}/node {} send append entries request to {} failed, position is {}",
                        topicPartitionGroup, localReplicaId, replica.replicaId(),
                        appendEntriesRequest.getStartPosition(), cause);

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
                logger.info("Partition group {}/node {} replicate consume pos to {} success",
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
        boolean success = true;
        long startTime = SystemClock.now();
        int entriesLength = request.getEntries().remaining();

        logger.debug("Partition group {}/node {} receive append entries request {}, start position " +
                    "is {}, write position is {}, commit position is {}",
                topicPartitionGroup, localReplicaId, request, startPosition, replicableStore.rightPosition(), commitPosition);
        do {
            try {
                if (state != ElectionNode.State.FOLLOWER) {
                    logger.info("Partition group {}/node {} receive append entries request, state is {}",
                            topicPartitionGroup, localReplicaId, state);
                    success = false;
                    break;
                }

                long matchPosition = matchPosition(request.getStartPosition(), request.getLeftPosition(),
                        request.getPrevTerm(), request.getPrevPosition(), request.isMatch());
                if (matchPosition != request.getStartPosition()) {
                    nextPosition = matchPosition;
                    success = false;
                    break;
                }

                CallerInfo info = null;
                try {
                    String key = "com.jd.journalq.replicate.appendEntries." + topicPartitionGroup + "." +
                            getReplica(localReplicaId).getIp();
                    info = Profiler.registerInfo(key, false, true);

                    if (SystemClock.now() - lastLogTime > electionConfig.getLogInterval()) {
                        logger.info("Partition group {}/node {}, append entries from {}, position is {}, entry length is {}",
                                topicPartitionGroup, localReplicaId, request.getLeaderId(), startPosition,
                                request.getEntries().remaining());
                        lastLogTime = SystemClock.now();
                    }

                    nextPosition = replicableStore.appendEntryBuffer(request.getEntries());

                } finally {
                    Profiler.registerInfoEnd(info);
                }

                brokerMonitor.onAppendReplicateMessage(topicPartitionGroup.getTopic(), topicPartitionGroup.getPartitionGroupId(),
                        1, request.getEntriesLength(), SystemClock.now() - startTime);

                commitPosition = request.getCommitPosition();

            } catch (Throwable t) {
                logger.warn("Partition group {}/node {} append entries to position {} failed, write position is {}， " +
                            "entries length is {}",
                        topicPartitionGroup, localReplicaId, startPosition, replicableStore.rightPosition(), entriesLength, t);
                success = false;
                nextPosition = -1L;
                break;
            }
        } while(false);

        logger.debug("Partition group {}/node {} append entires, start position is {}, write position " +
                    "is {}, commit position is {}, next position is {}",
                topicPartitionGroup, localReplicaId, startPosition, replicableStore.rightPosition(), commitPosition, nextPosition);

        AppendEntriesResponse response = AppendEntriesResponse.Build.create().topicPartitionGroup(topicPartitionGroup)
                .term(currentTerm).writePosition(replicableStore.rightPosition()).nextPosition(nextPosition)
                .replicaId(localReplicaId).success(success).build();

        return new Command(new JMQHeader(Direction.RESPONSE, CommandType.RAFT_APPEND_ENTRIES_RESPONSE), response);
    }

    /**
     * Match the log on leader and follower
     * @param position  position of the log to be compare
     * @param leftPosition left position of the leader logs
     * @param prevTerm  previous log's term
     * @param prevPosition  previous log's start position
     * @return matched position
     * @throws Exception exception
     */
    private long matchPosition(long position, long leftPosition, int prevTerm, long prevPosition, boolean isMatch) throws Exception {
        long writePosition = replicableStore.rightPosition();

        // If follower replica's write position less than start position of the message
        // return write position as next match position
        if (position > writePosition) {
            logger.info("Partition group {}/node {} match position, position is {}, write position is {}, " +
                        "left position is {}",
                    topicPartitionGroup, localReplicaId, position, writePosition, leftPosition);
            if (position == leftPosition){
                // If position equals left position, all logs in the follower should be removed and start replicate
                // logs from position
                replicableStore.setRightPosition(position, electionConfig.getDisableStoreTimeout());
                return position;
            } else if (writePosition > leftPosition) {
                // If write position bigger than left position, next position should be write position
                return writePosition;
            } else {
                // If write position less than left position, next position should be left position
                return leftPosition;
            }
        }

        if (position < writePosition) {
            logger.info("Partition group {}/node {} match position, position is {}, write position is {}" +
                        ", rollback to position {}",
                    topicPartitionGroup, localReplicaId, position, writePosition, position);
            replicableStore.setRightPosition(position, electionConfig.getDisableStoreTimeout());
        }

        if (isMatch) return position;

        // position equals left position
        if (prevPosition == leftPosition) {
            logger.info("Partition group {}/node {} match position, previous position {} equals left position {}",
                    topicPartitionGroup, localReplicaId, prevPosition, leftPosition);
            return position;
        }

        long localPrevPosition = 0;
        int localPrevTerm = 0;
        if (position > replicableStore.leftPosition()) {
            localPrevPosition = replicableStore.position(position, -1);
            localPrevTerm = replicableStore.getEntryTerm(localPrevPosition);
        }

        logger.info("Partition group {}/node {} match prev position and term, position is {}, left position is {}, " +
                    "prev position is {}, prev term is {}, local prev position is {}, local prev term is {}",
                topicPartitionGroup, localReplicaId, position, leftPosition,
                prevPosition, prevTerm, localPrevPosition, localPrevTerm);

        // if term of local previous position not match the leader
        // should match previous message
        if (prevPosition != localPrevPosition || prevTerm != localPrevTerm) {
            logger.info("Partition group {}/node {} prev position or term not match, position is {}, left position is {}, " +
                        "prev position is {}, prev term is {}, local prev term is {}, local prev position is {}",
                    topicPartitionGroup, localReplicaId, position, leftPosition,
                    prevPosition, prevTerm, localPrevTerm, localPrevPosition);
            return -1L;
        }
        return position;
    }

    /**
     * Process the response of append entries request
     * Update the commit position as the majority value of all replica's write position
     * @param response 写入记录响应
     */
    private synchronized void processAppendEntriesResponse(AppendEntriesResponse response, Replica replica) {

        if (!response.isSuccess()) {
            if (response.getNextPosition() == -1L) {
                replica.nextPosition(getPrevPosition(replica.nextPosition()));
            } else {
                replica.nextPosition(response.getNextPosition());
            }
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
        this.commitPosition = commitPosition;

        if (SystemClock.now() - lastLogTime > electionConfig.getLogInterval()) {
            replicasWithoutLearners.forEach(r -> logger.debug("Partition group {}/node {}", topicPartitionGroup, r));
            logger.info("Partition group {}/node {} commit position is {}",
                    topicPartitionGroup, localReplicaId, commitPosition);
            lastLogTime = SystemClock.now();
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

        logger.info("Partition group {}/node {} transer leadership to {}, log position is {}, " +
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
            if (!response.isSuccess()) {
                logger.info("Partition group {}/node {} timeout now request return success response",
                        topicPartitionGroup, localReplicaId);
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
        JMQHeader header = new JMQHeader(Direction.REQUEST, CommandType.RAFT_TIMEOUT_NOW_REQUEST);

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
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayTimeNs - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed another) {
            return Long.compare(delayTimeNs, ((DelayedCommand)another).delayTimeNs);
        }

        int replicaId() {
            return replicaId;
        }
    }

}
