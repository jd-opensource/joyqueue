package org.joyqueue.broker.store;

import com.jd.laf.extension.Extension;

/**
 * ClusterStoreService
 * author: gaohaoxiang
 * date: 2020/3/19
 */
// 临时组合类，切换存储后删除
@Extension(order = 0)
public class ClusterStoreService
        //extends Service implements StoreService, LifeCycle, Closeable, PropertySupplierAware, BrokerContextAware
 {

//    private StoreService storeService;
//    private BrokerContext brokerContext;
//    private ElectionService electionService;
//
//    private EventBus eventBus = new EventBus("joyqueue-cluster-store-eventBus");
//
//    protected StoreService loadStoreService() {
//        Iterator<StoreService> storeServiceIterator = Plugins.STORE.extensions().iterator();
//        while (storeServiceIterator.hasNext()) {
//            StoreService storeService = storeServiceIterator.next();
//            if (!storeService.getClass().equals(getClass())) {
//                return storeService;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public void setBrokerContext(BrokerContext brokerContext) {
//        this.brokerContext = brokerContext;
//    }
//
//    @Override
//    protected void validate() throws Exception {
//        this.electionService = brokerContext.getElectionService();
//        this.electionService.addListener((event) -> {
//            LeaderElection leaderElection = this.electionService.getLeaderElection(TopicName.parse(event.getTopicPartitionGroup().getTopic()), event.getTopicPartitionGroup().getPartitionGroupId());
//            if (leaderElection == null) {
//                return;
//            }
//            StoreNodes storeNodes = convertElectionNodes(leaderElection);
//            eventBus.add(new StoreNodeChangeEvent(event.getTopicPartitionGroup().getTopic(), event.getTopicPartitionGroup().getPartitionGroupId(), storeNodes));
//        });
//    }
//
//    @Override
//    protected void doStart() throws Exception {
//        if (storeService instanceof LifeCycle) {
//            ((LifeCycle) storeService).start();
//        }
//        eventBus.start();
//    }
//
//    @Override
//    protected void doStop() {
//        if (storeService instanceof LifeCycle) {
//            ((LifeCycle) storeService).stop();
//        }
//        eventBus.stop();
//    }
//
//    @Override
//    public void close() throws IOException {
//        if (storeService instanceof Closeable) {
//            ((Closeable) storeService).close();
//        }
//    }
//
//    @Override
//    public void setSupplier(PropertySupplier supplier) {
//        this.storeService = loadStoreService();
//        Preconditions.checkArgument(storeService != null, "storeService can not be null.");
//
//        if (storeService instanceof PropertySupplierAware) {
//            ((PropertySupplierAware) storeService).setSupplier(supplier);
//        }
//    }
//
//    @Override
//    public StoreNodes getNodes(String topic, int partitionGroup) {
//        LeaderElection leaderElection = electionService.getLeaderElection(TopicName.parse(topic), partitionGroup);
//        if (leaderElection == null) {
//            return null;
//        }
//        return convertElectionNodes(leaderElection);
//    }
//
//    protected StoreNodes convertElectionNodes(LeaderElection leaderElection) {
//        Collection<DefaultElectionNode> electionNodes = leaderElection.getAllNodes();
//        List<StoreNode> nodes = Lists.newArrayListWithCapacity(electionNodes.size());
//        for (DefaultElectionNode electionNode : electionNodes) {
//            boolean isLeader = electionNode.getNodeId() == leaderElection.getLeaderId();
//            nodes.add(new StoreNode(electionNode.getNodeId(), isLeader, isLeader));
//        }
//        return new StoreNodes(nodes);
//    }
//
//    @Override
//    public void addListener(EventListener<StoreEvent> listener) {
//        eventBus.addListener(listener);
//    }
//
//    @Override
//    public void removeListener(EventListener<StoreEvent> listener) {
//        eventBus.removeListener(listener);
//    }
//
//    @Override
//    public boolean partitionGroupExists(String topic, int partitionGroup) {
//        return storeService.partitionGroupExists(topic, partitionGroup);
//    }
//
//    @Override
//    public boolean topicExists(String topic) {
//        return storeService.topicExists(topic);
//    }
//
//    @Override
//    public TransactionStore getTransactionStore(String topic) {
//        return storeService.getTransactionStore(topic);
//    }
//
//    @Override
//    public List<TransactionStore> getAllTransactionStores() {
//        return storeService.getAllTransactionStores();
//    }
//
//    @Override
//    public void removePartitionGroup(String topic, int partitionGroup) {
//        storeService.removePartitionGroup(topic, partitionGroup);
//    }
//
//    @Override
//    public void restorePartitionGroup(String topic, int partitionGroup) throws Exception {
//        storeService.restorePartitionGroup(topic, partitionGroup);
//    }
//
//    @Override
//    public void createPartitionGroup(String topic, int partitionGroup, short[] partitions) throws Exception {
//        storeService.createPartitionGroup(topic, partitionGroup, partitions);
//    }
//
//    @Override
//    public PartitionGroupStore getStore(String topic, int partitionGroup, QosLevel writeQosLevel) {
//        return storeService.getStore(topic, partitionGroup, writeQosLevel);
//    }
//
//    @Override
//    public PartitionGroupStore getStore(String topic, int partitionGroup) {
//        return storeService.getStore(topic, partitionGroup);
//    }
//
//    @Override
//    public List<PartitionGroupStore> getStore(String topic) {
//        return storeService.getStore(topic);
//    }
//
//    @Override
//    public void rePartition(String topic, int partitionGroup, Short[] partitions) throws IOException {
//        storeService.rePartition(topic, partitionGroup, partitions);
//    }
//
//    @Override
//    public ReplicableStore getReplicableStore(String topic, int partitionGroup) {
//        return storeService.getReplicableStore(topic, partitionGroup);
//    }
//
//    @Override
//    public StoreManagementService getManageService() {
//        return storeService.getManageService();
//    }
//
//    @Override
//    public BufferPoolMonitorInfo monitorInfo() {
//        return storeService.monitorInfo();
//    }
}