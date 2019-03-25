package com.jd.journalq.broker.store;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Replica;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.config.PropertySupplier;
import com.jd.journalq.toolkit.config.PropertySupplierAware;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 存储管理器
 */
public class StoreManager extends Service implements BrokerContextAware, PropertySupplierAware {
    private final static Logger logger = LoggerFactory.getLogger(StoreManager.class);

    private BrokerContext brokerContext;
    private PropertySupplier propertySupplier;
    private NameService nameService;
    private ClusterManager clusterManager;
    private StoreService storeService;
    private StoreCleanManager storeCleanManager;
    private ElectionService electionService;

    public StoreManager(StoreService storeService , NameService nameService, ClusterManager clusterManager, ElectionService electionService) {
        this.storeService = storeService;
        this.nameService = nameService;
        this.clusterManager = clusterManager;
        this.electionService = electionService;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        Preconditions.checkArgument(brokerContext != null, "broker context can not be null");
        storeCleanManager = new StoreCleanManager(
                propertySupplier,
                storeService,
                clusterManager,
                this.brokerContext.getPositionManager()
        );
    }

    @Override
    public void doStop() {
        super.stop();
        storeCleanManager.stop();
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
        int brokerId = clusterManager.getBrokerId();
        List<Replica> replicas = nameService.getReplicaByBroker(brokerId);
        if (null != replicas) {
            for (Replica replica : replicas) {
                PartitionGroup group = clusterManager.getPartitionGroupByGroup(replica.getTopic(),replica.getGroup());
                logger.info("begin restore topic {},group.no {} group {}",replica.getTopic().getFullName(),replica.getGroup(),group);
                if (group.getReplicas().contains(brokerId)) {
                    storeService.restorePartitionGroup(group.getTopic().getFullName(), group.getGroup());
                    //electionService.onPartitionGroupCreate(group.getElectType(), group.getTopic(), group.getGroup(),
                    //        new ArrayList<>(group.getBrokers().values()), group.getLearners(), brokerId, group.getLeader());
                }
            }
        }
        storeCleanManager.start();
    }

    public NameService getNameService() {
        return nameService;
    }

    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
    }
}
