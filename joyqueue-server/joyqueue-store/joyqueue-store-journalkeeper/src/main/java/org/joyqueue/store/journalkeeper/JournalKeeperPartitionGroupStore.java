package org.joyqueue.store.journalkeeper;

import com.google.common.collect.Lists;
import io.journalkeeper.core.BootStrap;
import io.journalkeeper.core.api.AdminClient;
import io.journalkeeper.core.api.ClusterConfiguration;
import io.journalkeeper.core.api.JournalEntry;
import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.core.api.ResponseConfig;
import io.journalkeeper.core.api.UpdateRequest;
import io.journalkeeper.core.api.VoterState;
import io.journalkeeper.exceptions.NotLeaderException;
import io.journalkeeper.journalstore.JournalStoreClient;
import io.journalkeeper.journalstore.JournalStoreServer;
import io.journalkeeper.utils.event.EventWatcher;
import org.joyqueue.store.journalkeeper.entry.JoyQueueEntryParser;
import org.joyqueue.store.journalkeeper.transaction.JournalKeeperTransactionStore;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.ReadResult;
import org.joyqueue.store.WriteRequest;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.message.MessageParser;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author LiYue
 * Date: 2019-09-19
 */
public class JournalKeeperPartitionGroupStore extends Service implements PartitionGroupStore {
    private static final Logger logger = LoggerFactory.getLogger(JournalKeeperPartitionGroupStore.class);
    private final JournalStoreServer server;
    private final String topic;
    private final int group;
    private final EventWatcher eventWatcher;
    private JournalStoreClient client;
    private AdminClient adminClient;
    private TransactionStore transactionStore = null;
    private final ExecutorService asyncExecutor;
    private final JournalKeeperStore store;
    JournalKeeperPartitionGroupStore(JournalKeeperStore store,
            String topic,
            int group,
            RaftServer.Roll roll,
            EventWatcher eventWatcher,
            ExecutorService asyncExecutor,
            ScheduledExecutorService scheduledExecutor,
            Properties properties){
        this.store=store;
        this.topic = topic;
        this.group = group;
        this.eventWatcher = eventWatcher;
        this.asyncExecutor = asyncExecutor;
        server = new JournalStoreServer(roll, new JoyQueueEntryParser(),
                asyncExecutor, scheduledExecutor, asyncExecutor, scheduledExecutor, properties);
        //server.
    }

    @Override
    public boolean isInitialized() {
        return server.isInitialized();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        logger.info("JournalKeeper partition group store {} starting",this.getUri());
        server.start();
        this.client = server.createLocalClient();
        this.adminClient = server.getLocalAdminClient();
        this.transactionStore = new JournalKeeperTransactionStore(client);
        if(null != eventWatcher) {
            this.client.watch(eventWatcher);
        }
        logger.info("JournalKeeper partition group store {} started",this.getUri());
    }

    @Override
    protected void doStop() {
        super.doStop();
        if(null != eventWatcher && null != this.client) {
            this.client.unWatch(eventWatcher);
        }
        server.stop();
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int getPartitionGroup() {
        return group;
    }

    @Override
    public Short[] listPartitions() {
        try {
            Set<Integer> partitions = client.listPartitions().get();
            return partitions.stream().map(Integer::shortValue).toArray(Short[]::new);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLeftIndex(short partition) {
        try {
            Map<Integer, Long> map = client.minIndices().get();
            return map.getOrDefault((int) partition, -1L);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getRightIndex(short partition) {
        try {
            Map<Integer, Long> map = client.maxIndices().get();
            return map.getOrDefault((int) partition, -1L);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getIndex(short partition, long timestamp) {
        try {
            return client.queryIndex(partition, timestamp).get();
        } catch (Throwable e) {
            logger.warn("Query index exception: ", e);
            return -1L;
        }
    }

    @Override
    public CompletableFuture<WriteResult> asyncWrite(QosLevel qosLevel, WriteRequest... writeRequests) {
        List<UpdateRequest> updateRequests = Arrays.stream(writeRequests)
                .map(r -> new UpdateRequest(r.getBuffer().array(), r.getPartition(), r.getBatchSize()))
                .collect(Collectors.toList());
        return client.append(updateRequests, true, qosLevelToResponseConfig(qosLevel))
                .thenApply(indices -> new WriteResult(JoyQueueCode.SUCCESS, indices.stream().mapToLong(i -> i).toArray()))
                .exceptionally(e -> new WriteResult(JoyQueueCode.SE_WRITE_FAILED, new long [0])); // TODO: 精细化返回值，针对各种错误情况做映射
    }

    @Override
    public void asyncWrite(EventListener<WriteResult> eventListener, QosLevel qosLevel, WriteRequest... writeRequests) {
        asyncWrite(qosLevel, writeRequests)
            .thenAccept(writeResult -> {
                if(eventListener != null) {
                    eventListener.onEvent(writeResult);
                }
        });
    }



    @Override
    public ReadResult read(short partition, long index, int count, long maxSize) {
        try {

            return client.get(partition, index, count)
                    .thenApply(journalEntries -> {
                        ReadResult readResult = new ReadResult();
                        readResult.setCode(JoyQueueCode.SUCCESS);
                        ByteBuffer [] messageArray = new ByteBuffer[journalEntries.size()];
                        long currentIndex = index;
                        for (int i = 0; i < journalEntries.size(); i++) {
                            JournalEntry journalEntry = journalEntries.get(i);
                            byte [] message = journalEntry.getSerializedBytes();
                            ByteBuffer buffer = ByteBuffer.wrap(message);

                            // 读出来的消息中不含索引值，这里需要设置索引
                            currentIndex -= journalEntry.getOffset();
                            MessageParser.setLong(buffer, MessageParser.INDEX, currentIndex);
                            currentIndex += journalEntry.getBatchSize();

                            messageArray[i] = buffer;
                        }

                        readResult.setMessages(messageArray);
                        return readResult;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Read exception, topic: {}, group: {}, partition: {}, index: {}, count: {}, maxSize: {}.",
                    topic, group, partition, index, count, maxSize, e);
            // TODO 细化异常处理
            ReadResult readResult = new ReadResult();
            readResult.setCode(JoyQueueCode.SE_READ_FAILED);
            return readResult;
        }
    }

    @Override
    public boolean readable() {
        return writable();
    }

    @Override
    public boolean writable() {
        try {
            return adminClient.getServerStatus(getUri()).get().getVoterState() == VoterState.LEADER;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> whenClusterReady(long timeoutMs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                client.waitForClusterReady(timeoutMs);
                return true;
            } catch (TimeoutException ignored) {
                return false;
            }
        });
    }


    private ResponseConfig qosLevelToResponseConfig(QosLevel qosLevel) {
        switch (qosLevel) {
            case ONE_WAY: return ResponseConfig.ONE_WAY;
            case RECEIVE: return ResponseConfig.RECEIVE;
            case PERSISTENCE: return ResponseConfig.PERSISTENCE;
            case ALL: return ResponseConfig.ALL;
            default: return ResponseConfig.REPLICATION;
        }
    }

    void restore() throws IOException {
        server.recover();
    }

    void init(List<URI> uriList, URI thisServer, short [] partitions) throws IOException {
        Set<Integer> partitionSet = new HashSet<>(partitions.length);
        for (short partition : partitions) {
            partitionSet.add((int) partition);
        }
        server.init(
                thisServer,
                uriList,
                partitionSet
        );
    }

    void maybeRePartition(Collection<Short> partitions) {
        client.listPartitions().thenAccept(storePartitions -> {
            Set<Short> shortPartitionSet = storePartitions.stream().map(Integer::shortValue).collect(Collectors.toSet());
            if (!shortPartitionSet.containsAll(partitions)) {
                rePartition(partitions);
            }
        });


    }

    /**
     *
     * @param id  preferred broker id
     */
    void updatePreferredLeader(URI id) throws Exception{
        try {
            adminClient.setPreferredLeader(id).get();
        }catch (Exception e){
            logger.info("Update preferred leader exception {}",id,e);
            throw e;
        }
    }

    private void rePartition(Collection<Short> partitions) {
        adminClient.scalePartitions(partitions.stream().mapToInt(p -> (int) p).boxed().collect(Collectors.toSet()))
                .whenComplete((aVoid, exception) -> {
                    if(null != exception) {
                        if(exception instanceof NotLeaderException) {
                            logger.info("Ignore scale partition command, I'm not the leader. Topic: {}, group: {}, new partitions: {}.",
                                    getTopic(), getPartitionGroup(), partitions);
                        } else {
                            logger.warn("Scale partition failed! Topic: {}, group: {}, new partitions: {}.",
                                    getTopic(), getPartitionGroup(), partitions);
                        }
                    } else {
                        logger.info("Scale partition success! Topic: {}, group: {}, new partitions: {}.",
                                getTopic(), getPartitionGroup(), partitions);
                    }
                });
    }

    void maybeUpdateConfig(List<URI> newConfigs) {
        adminClient
            .getClusterConfiguration(server.serverUri())
            .thenAccept(clusterConfiguration -> {
                List<URI> voters= Lists.newArrayList(clusterConfiguration.getVoters());
                if(!(voters.containsAll(newConfigs)&&newConfigs.containsAll(voters))) {
                    if(server.serverUri().equals(clusterConfiguration.getLeader())) {
                        // leader
                        updateConfig(newConfigs,voters);
                        logger.info("Current leader {},local {} update cluster config.\n old {}\n new {}", clusterConfiguration.getLeader(), server.serverUri(),
                                clusterConfiguration.getVoters(), newConfigs);
                    }
                }else{
                    logger.warn("No difference,ignore new configs{}, old configs {} on {}",newConfigs,voters,server.serverUri());
                }
                mayWaitNewConfigReady(newConfigs,30,TimeUnit.SECONDS);
            });

    }

    /**
     *
     * 等待新的集群配置生效,目前只检查Leader节点是否已变更为最新配置
     * 如果等待失败需要补偿
     **/
    public CompletableFuture<Boolean> mayWaitNewConfigReady(List<URI> newConfigs, long timeout, TimeUnit unit){
        // 本节点被删除
        logger.info("Start to waiting for new config {} on {}",newConfigs,server.serverUri());
        CompletableFuture<Boolean> result=new CompletableFuture();
        CompletableFuture.runAsync(()-> {
            if (!newConfigs.contains(server.serverUri())) {
                logger.info("Start to waiting for new config {} on {}",newConfigs,server.serverUri());
                if(newConfigs.size()>0) {
                    AdminClient newAdminClient = new BootStrap(newConfigs, new Properties()).getAdminClient();
                    //轮询新集群的配置
                    Set<URI> newConfigSet = new HashSet<>(newConfigs);
                    long startMs = SystemClock.now();
                    long finalTimeout = startMs + unit.toMillis(timeout);
                    do {
                        try {
                            ClusterConfiguration clusterConfiguration = newAdminClient.getClusterConfiguration().get(100, TimeUnit.MILLISECONDS);
                            Set<URI> leaderVoterSet = new HashSet<>(clusterConfiguration.getVoters());
                            if (leaderVoterSet.size() == newConfigSet.size() && newConfigSet.containsAll(leaderVoterSet)) {
                                store.removePartitionGroup(topic, group);
                                result.complete(true);
                                newAdminClient.stop();
                                break;
                            }
                            if (SystemClock.now() > finalTimeout) {
                                logger.warn("Failed to wait new config on {}", server.serverUri());
                                result.complete(false);
                                newAdminClient.stop();
                                break;
                            }
                            Thread.sleep(100);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            logger.warn("Wait new config exception ", e);
                        }
                    } while (true);
                }else{
                    logger.info("New config is empty,remove {}/{} right now!",topic,group);
                    store.removePartitionGroup(topic, group);
                }
            }else{
                logger.info("New config contain me {}",server.serverUri());
            }
        });
        return result;
    }


    private void updateConfig(List<URI> newConfigs, List<URI> oldConfigs) {
        adminClient
            .updateVoters(oldConfigs, newConfigs)
            .whenComplete((success, exception) -> {
                if(null != exception) {
                    if(exception instanceof NotLeaderException) {
                        logger.info("Ignore update config command, I'm not the leader. Topic: {}, group: {}, new configs: {}.",
                                getTopic(), getPartitionGroup(), newConfigs);
                    } else {
                        logger.warn("Update config failed! Topic: {}, group: {}, new configs: {}.",
                                getTopic(), getPartitionGroup(), newConfigs, exception);
                    }
                } else if(success) {
                    logger.info("Update config success! Topic: {}, group: {}, new configs: {}.",
                            getTopic(), getPartitionGroup(), newConfigs);
                } else {
                    logger.warn("Update config failed! Topic: {}, group: {}, new configs: {}.",
                            getTopic(), getPartitionGroup(), newConfigs);
                }
            });
    }

    URI getUri() {
        return server.serverUri();
    }

    TransactionStore getTransactionStore() {
        return transactionStore;
    }


}
