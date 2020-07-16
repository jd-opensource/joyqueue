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
package org.joyqueue.store;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.config.PartitionGroupConfig;
import org.joyqueue.broker.helper.AwareHelper;
import org.joyqueue.broker.store.StoreUtils;
import org.joyqueue.domain.*;
import org.joyqueue.helper.PortHelper;
import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.network.transport.config.TransportConfigSupport;
import org.joyqueue.store.network.BackendServer;
import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.store.ha.ReplicableStore;
import org.joyqueue.store.ha.election.DefaultElectionNode;
import org.joyqueue.store.ha.election.ElectionManager;
import org.joyqueue.store.ha.election.ElectionMetadata;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.store.transaction.TransactionStoreManager;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liyue25
 * Date: 2018/8/13
 * <p>
 * root                            # 数据文件根目录
 * ├── metadata                    # 元数据，目前只存放brokerId
 * ├── lock                        # 进程锁目录，避免多进程同时操作导致数据损坏
 * │   └── 112334                  # 当前持有锁的进程的PID
 * └── topics                      # 所有topic目录，子目录就是topic名称
 * ├── coupon                  # topic coupon
 * └── order                   # topic order
 * ├── 1                   # topic group
 * │   ├── checkpoint      # 检查点文件
 * │   ├── index           # 索引文件目录，每个子目录为partition，目录名称就是partitionId
 * │   │   ├── 3           # partition 3，目录下的文件为该partition的索引文件
 * │   │   │   ├── 1048576 # 索引文件，固定文件长度，文件名为文件记录的第一条消息索引在Partition中的序号。
 * │   │   │   └── 2097152
 * │   │   ├── 4
 * │   │   └── 5
 * │   ├── 0               # 消息日志文件
 * │   ├── 134217728
 * │   └── 268435456
 * ├── tx                  # 事务消息目录，存放未提交的事务消息
 * │   ├── 0
 * │   ├── 1
 * │   └── 2
 * └── subscription        # 订阅文件，记录所有订阅和消费指针
 */
public class Store extends Service implements StoreService, Closeable, PropertySupplierAware, BrokerContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(Store.class);
    private static final int SCHEDULE_EXECUTOR_THREADS = 16;
    private static final String FORWARD_SLASH ="/";

    private static final String TOPICS_DIR = "topics";
    private static final String TX_DIR = "tx";
    private static final String DEL_PREFIX = ".d.";
    /**
     * key: [topic]/[group index]，例如：order/1
     */
    private final Map<String, PartitionGroupStoreManager> storeMap = new HashMap<>();
    private final Map<String, TransactionStoreManager> txStoreMap = new HashMap<>();
    private StoreConfig config;
    private PreloadBufferPool bufferPool;
    private File base;
    private PropertySupplier propertySupplier;
    private BrokerContext brokerContext;
    private StoreLock storeLock;
    /**
     * Election module
     **/
    private ElectionManager electionManager;
    private ClusterManager clusterManager;
    private BackendServer backendServer;
    public Store() {
        //do nothing
    }

    public Store(StoreConfig config) {
        this.config = config;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (config == null) {
            config = new StoreConfig(propertySupplier);
        }
        if (base == null) {
            base = new File(config.getPath()+File.separator+name());
        }
        checkOrCreateBase();
        if (storeLock == null) {
            storeLock = new StoreLock(new File(base, "lock"));
            storeLock.lock();
        }
        if (bufferPool == null) {
            System.setProperty(PreloadBufferPool.PRINT_METRIC_INTERVAL_MS_KEY, String.valueOf(config.getPrintMetricIntervalMs()));
            this.bufferPool = PreloadBufferPool.getInstance();
        }
        this.bufferPool.addPreLoad(config.getIndexFileSize(), config.getPreLoadBufferCoreCount(), config.getPreLoadBufferMaxCount());
        this.bufferPool.addPreLoad(config.getMessageFileSize(), config.getPreLoadBufferCoreCount(), config.getPreLoadBufferMaxCount());
        if(clusterManager==null){
            this.clusterManager=brokerContext.getClusterManager();
        }
        // validate election service
        if(electionManager==null){
            electionManager=new ElectionManager(this);
            AwareHelper.enrichIfNecessary(electionManager,brokerContext);
        }
    }

    /**
     *  Start storage and election
     **/
    @Override
    protected void doStart() throws Exception {
        super.doStart();

        LOG.info("Starting store {}...", base.getPath());

        for (PartitionGroupStoreManager manger : storeMap.values()) {
            if (!manger.isStarted()) manger.start();
        }
        started.set(true);

        LOG.info("Store started.");
        // start backend server

        startBackEndServer();
        LOG.info("Store started.");
        if(!electionManager.isStarted()) {
            electionManager.start();
        }
        LOG.info("Election started.");
    }

    public void startBackEndServer() throws Exception{
        // 使用存储层端口
        ServerConfig backendConfig = TransportConfigSupport.buildServerConfig(propertySupplier,"store.");
        backendConfig.setAcceptThreadName("joyqueue-backend-accept-eventLoop");
        backendConfig.setIoThreadName("joyqueue-backend-io-eventLoop");
        backendConfig.setPort(PortHelper.getStorePortOffset(brokerContext.getBroker().getPort()));
        this.backendServer = new BackendServer(backendConfig, brokerContext,electionManager);
        backendServer.start();
    }

    @Override
    protected void doStop() {
        super.doStop();
        LOG.info("Stopping backend sever...");
        backendServer.stop();
        LOG.info("Stopping store {}...", base.getPath());

        storeMap.values().forEach(p -> {
            p.disable();
            p.stop();
        });

        storeLock.unlock();

        LOG.info("Store {} stopped.", base.getPath());
    }

    public void checkOrCreateBase() {
        if (!base.exists()) {
            if (!base.mkdirs()) throw new StoreInitializeException(String.format("Failed to create directory: %s.", base.getAbsolutePath()));
        } else {
            if (!base.isDirectory()) throw new StoreInitializeException(String.format("Failed to create directory: %s! Cause: file exists!", base.getAbsolutePath()));
        }
    }

    @Override
    public TransactionStore getTransactionStore(String topic, short partition) {
        return getTransactionStore(topic);
    }

    @Override
    public PartitionGroupStore restoreOrCreatePartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokers,
                                                             List<Integer> observers, int thisBrokerId,PropertySupplier extend) throws Exception{
        if(partitionGroupExists(topic,partitionGroup)){
            restorePartitionGroup(topic,partitionGroup);
            electionManager.onPartitionGroupRestore(new TopicName(topic),partitionGroup);
        }else{
            createPartitionGroup(topic,partitionGroup,partitions,brokers,observers,thisBrokerId,extend);
        }
        return getStore(topic,partitionGroup);
    }

    @Override
    public PartitionGroupStore createPartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokers,
                                                    List<Integer> observers, int thisBrokerId,PropertySupplier extend) throws Exception{
        createPartitionGroup(topic,partitionGroup,partitions);
        PartitionGroupConfig config=new PartitionGroupConfig(extend);
        electionManager.onPartitionGroupCreate(config.electionType(),new TopicName(topic),partitionGroup,brokers(brokers),new HashSet(observers),thisBrokerId,config.fixLeader());
        if(config.electionType()== PartitionGroup.ElectType.raft&&config.recommendLeader()>0){
            updatePreferredLeader(topic,partitionGroup,config.recommendLeader());
        }
        return getStore(topic,partitionGroup);
    }



    @Override
    public void stopPartitionGroup(String topic, int partitionGroup) {
        PartitionGroupStoreManager partitionGroupStoreManager= storeMap.get(partitionGroupStoreKey(topic,partitionGroup));
        if(Objects.nonNull(partitionGroupStoreManager)){
            partitionGroupStoreManager.stop();
            electionManager.onPartitionGroupStop(TopicName.parse(topic),partitionGroup);
        }
    }

    @Override
    public Collection<PartitionGroupStore> getAllStores() {
        return storeMap.values().stream().map(e->e.getQosStore(QosLevel.REPLICATION)).collect(Collectors.toList());
    }

    /**
     *  理论上不会同时增加和删除节点,且一次只增加/减少一个节点
     *  副本变更如果不涉及当前节点，相当于只改变当前节点主题 分组的配置；
     *  如果新增/删除的是本节点，则需要改变相应的存储/选举
     *  目前没有考虑 learner 的更新
     **/
    @Override
    public void maybeUpdateReplicas(String topic, int partitionGroup, Collection<Integer> newReplicaBrokerIds) throws Exception{
        if(LOG.isDebugEnabled()){
            LOG.info("{}/{} replicas config change,new config {} on {}",topic,partitionGroup,newReplicaBrokerIds==null?"":newReplicaBrokerIds.toArray(),
                    clusterManager.getBrokerId());
        }
        Integer local=clusterManager.getBrokerId();
        if(newReplicaBrokerIds.contains(local)) {
            PartitionGroupStoreManager pgm = storeMap.get(partitionGroupStoreKey(topic, partitionGroup));
            PartitionGroup groupOld= clusterManager.getPartitionGroupByGroup(TopicName.parse(topic),partitionGroup);
            if (pgm == null) {
                  if(groupOld!=null){
                      createPartitionGroup(topic,partitionGroup, Shorts.toArray(groupOld.getPartitions()),Lists.newArrayList(newReplicaBrokerIds),
                              Lists.newArrayList(groupOld.getLearners()),local, StoreUtils.partitionGroupExtendProperties(groupOld));
                      LOG.info("Create partition group {}/{} new replica by scale up on {} ",topic,partitionGroup,local);
                  }else{
                      LOG.warn("Partition group {}/{} metadata  missing on {}",topic,partitionGroup,local);
                  }
            }else {
                electionManager.onNodeChange(TopicName.parse(topic), partitionGroup,brokers(Lists.newArrayList(newReplicaBrokerIds)),local);
                LOG.info("Partition group exist on {}, may update replicas",local);
            }
        }else{
            // local 被移除
            PartitionGroupStoreManager pgm = storeMap.get(partitionGroupStoreKey(topic, partitionGroup));
            if(pgm!=null){
                removePartitionGroup(topic,partitionGroup);
                LOG.info("Partition group {}/{} remove by scale down on {} ",topic,partitionGroup,local);
            }
        }
        /*if(newReplicaBrokerIds.con)

        Set<Integer> replicasNew = new HashSet<>(newReplicaBrokerIds);

        Set<Integer> replicasRemoved=new HashSet<>(groupOld.getReplicas());
        replicasRemoved.removeAll(replicasNew);

        Set<Integer> replicasNewAdd= new HashSet<>(replicasNew);
        replicasNewAdd.removeAll(groupOld.getReplicas());
        for(Integer add:replicasNewAdd){
            // 目前没有learner,暂时忽略learner的更新
            Broker cur = clusterManager.getBrokerById(add);
            if (null!=cur) {
                if (add.equals(local)){
                }else{
                    electionManager.onNodeAdd(TopicName.parse(topic), partitionGroup, groupOld.getElectType(),
                            brokers(Lists.newArrayList(newReplicaBrokerIds)), groupOld.getLearners(), cur,local,groupOld.getLeader());
                }
            }else{
                LOG.warn("Ignored,New node {} not found on metadata ",add);
            }
            LOG.info("Topic[{}] update partitionGroup[{}] and add node[{}] ", topic, partitionGroup,add);
        }
        for(Integer r:replicasRemoved){
            if(r.equals(local)){
                removePartitionGroup(topic,partitionGroup);
            }else {
                electionManager.onNodeRemove(TopicName.parse(topic), partitionGroup, r, local);
            }
            LOG.info("Topic[{}] update partitionGroup[{}] and remove node[{}] ", topic, partitionGroup, r);
        }*/
    }

    /**
     *  Partition group from local
     *  以当前的配置为准
     *
     **/
    private PartitionGroup partitionGroup(String topic,int partitionGroup){
      ElectionMetadata electionMetadata= electionManager.getMetadata(TopicName.parse(topic),partitionGroup);
      PartitionGroup pg=new PartitionGroup();
      if(electionMetadata!=null){
          pg.setReplicas(electionMetadata.getAllNodes().stream().map(DefaultElectionNode::getNodeId).collect(Collectors.toSet()));
          pg.setLearners(electionMetadata.getLearners());
          pg.setElectType(electionMetadata.getElectType());
          pg.setLeader(pg.getLeader());
          pg.setRecLeader(pg.getRecLeader());
      }else{
          pg.setReplicas(Collections.EMPTY_SET);
          pg.setLearners(Collections.EMPTY_SET);
      }
      return pg;
    }

    @Override
    public void updatePreferredLeader(String topic, int partitionGroup, int brokerId) throws Exception{
        electionManager.onLeaderChange(TopicName.parse(topic),partitionGroup,brokerId);
    }

    @Override
    public List<TransactionStore> getTransactionStores(String topic) {
        return Lists.newArrayList(getTransactionStore(topic));
    }

    public boolean physicalDelete() {
        if (started.get()) {
            LOG.info("Stop me fist!");
            return false;
        } else {
            LOG.info("PHYSICAL DELETE {}...", base.getAbsolutePath());
            deleteFolder(base);
            return true;
        }
    }

    private boolean partitionGroupExists(String topic, int partitionGroup) {
        return new File(base, getPartitionGroupRelPath(topic, partitionGroup)).isDirectory();
    }


    private boolean topicExists(String topic) {
        return new File(base, getTopicRelPath(topic)).isDirectory();
    }


    /**
     *
     * Transaction store for all partition of local partition
     *
     **/
    private TransactionStore getTransactionStore(String topic) {
        synchronized (txStoreMap) {
            if (txStoreMap.containsKey(topic)) {
                return txStoreMap.get(topic);
            } else {
                File txBase = new File(new File(base, getTopicRelPath(topic)), TX_DIR);
                if (topicExists(topic) && (txBase.isDirectory() || txBase.mkdirs())) {
                    TransactionStoreManager transactionStore =
                            new TransactionStoreManager(txBase,
                                    getMessageStoreConfig(config), bufferPool);
                    txStoreMap.put(topic, transactionStore);
                    return transactionStore;
                } else {
                    return null;
                }
            }
        }


    }

    @Override
    public List<TransactionStore> getAllTransactionStores() {
        return this.storeMap.keySet().stream()
                .map(key -> key.replaceAll("^(.*)/\\d+$", "$1"))
                .distinct()
                .map(topic -> {
                    synchronized (txStoreMap) {
                        if (txStoreMap.containsKey(topic)) {
                            return txStoreMap.get(topic);
                        } else {
                            File txBase = new File(new File(base, getTopicRelPath(topic)), TX_DIR);
                            if (txBase.isDirectory()) {
                                TransactionStoreManager transactionStore = new TransactionStoreManager(txBase,
                                        getMessageStoreConfig(config), bufferPool);
                                txStoreMap.put(topic, transactionStore);
                                return transactionStore;
                            } else {
                                return null;
                            }
                        }
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public synchronized void removePartitionGroup(String topic, int partitionGroup) {
        PartitionGroupStoreManager partitionGroupStoreManger = storeMap.remove(topic + "/" + partitionGroup);
        if (null != partitionGroupStoreManger) {
            partitionGroupStoreManger.stop();
            partitionGroupStoreManger.close();
        }
        electionManager.onPartitionGroupRemove(new TopicName(topic),partitionGroup);
        File groupBase = new File(base, getPartitionGroupRelPath(topic, partitionGroup));

        if (groupBase.exists()) delete(groupBase);


        File topicBase = new File(base, getTopicRelPath(topic));

        File[] files = topicBase.listFiles((dir, name) -> name.matches("\\d+"));
        if (null == files || files.length == 0) {
            synchronized (txStoreMap) {
                if (txStoreMap.containsKey(topic)) {
                    TransactionStoreManager transactionStore = txStoreMap.remove(topic);
                    if (null != transactionStore) {
                        transactionStore.close();
                    }
                }
            }
            delete(topicBase);
        }
        LOG.info("Remove partition group {}/{} storage",topic,partitionGroup);
    }


    /**
     * Restore partition group local store
     **/
    private synchronized void restorePartitionGroup(String topic, int partitionGroup) throws Exception{
        PartitionGroupStoreManager partitionGroupStoreManger = partitionGroupStore(topic, partitionGroup);
        if (null == partitionGroupStoreManger) {
            File groupBase = new File(base, getPartitionGroupRelPath(topic, partitionGroup));
            partitionGroupStoreManger = new PartitionGroupStoreManager(topic, partitionGroup, groupBase
                    , getPartitionGroupConfig(config)
                    , bufferPool);
            partitionGroupStoreManger.recover();
            if (isStarted()) {
                partitionGroupStoreManger.start();
            }
            storeMap.put(topic + "/" + partitionGroup, partitionGroupStoreManger);
            LOG.info("Restored partition group {}/{}",topic,partitionGroup);
        }else{
            LOG.warn("Partition group store manager already loaded");
        }
    }

    /**
     *  Init partition group store file, delete old if already exist
     *
     *
     **/
    public synchronized void createPartitionGroup(String topic, int partitionGroup, short[] partitions) throws Exception {
        if (!storeMap.containsKey(topic + "/" + partitionGroup)) {
            File groupBase = new File(base, getPartitionGroupRelPath(topic, partitionGroup));
            if (groupBase.exists()) delete(groupBase);
            PartitionGroupStoreSupport.init(groupBase, partitions);
            restorePartitionGroup(topic, partitionGroup);
            LOG.info("New partition group {}/{},{}",topic,partitionGroup,Arrays.toString(partitions));
        }
    }


    private PartitionGroupStoreManager.Config getPartitionGroupConfig(StoreConfig config) {

        PositioningStore.Config messageConfig = getMessageStoreConfig(config);
        PositioningStore.Config indexConfig = getIndexStoreConfig(config);
        return new PartitionGroupStoreManager.Config(
                config.getMaxMessageLength(), config.getWriteRequestCacheSize(), config.getFlushIntervalMs(),
                config.getWriteTimeoutMs(), config.getMaxDirtySize(),
                config.getPrintMetricIntervalMs(), messageConfig, indexConfig);
    }

    private PositioningStore.Config getIndexStoreConfig(StoreConfig config) {
        return new PositioningStore.Config(config.getIndexFileSize(),
                config.getFileHeaderSize(), config.getDiskFullRatio());
    }

    private PositioningStore.Config getMessageStoreConfig(StoreConfig config) {
        return new PositioningStore.Config(config.getMessageFileSize(),
                config.getFileHeaderSize(), config.getDiskFullRatio(),config.getMaxMessageLength());
    }

    /**
     * 并不真正删除，只是重命名
     */
    private boolean delete(File file) {
        File renamed = new File(file.getParent(), DEL_PREFIX + SystemClock.now() + "." + file.getName());
        return file.renameTo(renamed);
    }

    /**
     *  Qos partition group store
     **/
    public PartitionGroupStore getStore(String topic, int partitionGroup, QosLevel writeQosLevel) {
        PartitionGroupStoreManager partitionGroupStoreManager = partitionGroupStore(topic, partitionGroup);
        return partitionGroupStoreManager == null ? null : partitionGroupStoreManager.getQosStore(writeQosLevel);
    }

    /**
     *  Partition group store
     *
     **/
    @Override
    public PartitionGroupStore getStore(String topic, int partitionGroup) {
        return getStore(topic, partitionGroup, QosLevel.REPLICATION);
    }

    @Override
    public List<PartitionGroupStore> getStore(String topic) {
        return partitionGroupStores(topic).stream().map(p -> p.getQosStore(QosLevel.REPLICATION)).collect(Collectors.toList());
    }

    @Override
    public void maybeRePartition(String topic, int partitionGroup, Collection<Short> expectPartitions) throws IOException {
        PartitionGroupStoreManager partitionGroupStoreManger = partitionGroupStore(topic, partitionGroup);
        if (null != partitionGroupStoreManger) {
            partitionGroupStoreManger.rePartition(expectPartitions);
        } else {
            throw new NoSuchPartitionGroupException();
        }
    }

    /**
     * 获取用于选举复制的存储
     */
    public ReplicableStore replicableStore(String topic, int partitionGroup) {
        return partitionGroupStore(topic, partitionGroup);
    }

    /**
     * 获取管理接口
     */
    @Override
    public StoreManagementService getManageService() {

        return new StoreManagement(128, 128, config.getMaxMessageLength(), bufferPool, this);
    }

    @Override
    public BufferPoolMonitorInfo monitorInfo() {
        return bufferPool.monitorInfo();
    }

    /**
     * Replace @ char in topic
     *
     **/
    private String getPartitionGroupRelPath(String topic, int partitionGroup) {
        return TOPICS_DIR + File.separator + topic.replace('/', '@') + File.separator + partitionGroup;
    }

    private String getTopicRelPath(String topic) {
        return TOPICS_DIR + File.separator + topic;
    }


    protected File base() {
        return base;
    }



    // TODO: 在哪儿调用呢？
    @Override
    public void close() throws IOException {
        for (PartitionGroupStoreManager p : storeMap.values()) {
            p.close();
        }
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if (!f.delete()) {
                        LOG.warn("Delete failed: {}", f.getAbsolutePath());
                    }
                }
            }
        }
        if (!folder.delete()) {
            LOG.warn("Delete failed: {}", folder.getAbsolutePath());

        }
    }


    protected List<String> topics() {
        return storeMap.keySet().stream().map(k -> k.split("/")[0]).distinct().collect(Collectors.toList());
    }

    private List<String> partitionGroups() {
        return new ArrayList<>(storeMap.keySet());
    }

    protected List<Integer> partitionGroups(String topic) {
        return storeMap.keySet().stream()
                .filter(k -> k.matches("^" + topic + "/\\d+$"))
                .map(k -> k.substring(topic.length() + 1))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    protected PartitionGroupStoreManager partitionGroupStore(String topic, int partitionGroup) {
        return storeMap.get(partitionGroupStoreKey(topic,partitionGroup));
    }

    private List<PartitionGroupStoreManager> partitionGroupStores(String topic) {
        return partitionGroups(topic).stream().map(id -> storeMap.get(partitionGroupStoreKey(topic,id))).collect(Collectors.toList());
    }

    /**
     *
     * Partition group  key
     * @param topic
     * @param partitionGroup
     *
     **/
    public String partitionGroupStoreKey(String topic,int partitionGroup){
       return topic + FORWARD_SLASH + partitionGroup;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
        if (config == null) {
            config = new StoreConfig(supplier);
        }
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext=brokerContext;
    }


    @Override
    public String name(){
        return "JoyQueue";
    }

    /**
     *
     * Get broker by broker Id
     *
     **/
    private List<Broker> brokers(List<Integer> brokerIds){
        List<Broker> brokers = new ArrayList<>(brokerIds.size());
        brokerIds.forEach(brokerId -> {
            brokers.add(clusterManager.getBrokerById(brokerId));
        });
        return brokers;
    }
}
