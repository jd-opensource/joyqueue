package com.jd.journalq.broker;

import com.jd.journalq.broker.archive.ArchiveManager;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.broker.config.Configuration;
import com.jd.journalq.broker.config.ContextManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.coordinator.CoordinatorService;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.manage.BrokerManageService;
import com.jd.journalq.broker.manage.config.BrokerManageConfig;
import com.jd.journalq.broker.monitor.BrokerMonitorService;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.broker.monitor.config.BrokerMonitorConfig;
import com.jd.journalq.broker.network.BrokerServer;
import com.jd.journalq.broker.network.protocol.ProtocolManager;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.broker.retry.BrokerRetryManager;
import com.jd.journalq.broker.store.StoreManager;
import com.jd.journalq.common.domain.Config;
import com.jd.journalq.common.security.Authentication;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.config.Property;
import com.jd.journalq.toolkit.config.PropertySupplierAware;
import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;


/**
 * Created by chengzhiliang on 2018/9/27.
 */
public class BrokerServiceTest extends Service {

    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private static final String NAMESERVICE_NAME = "broker.nameservice.name";
    private BrokerConfig brokerConfig;
    private SessionManager sessionManager;
    private BrokerMonitorService brokerMonitorService;
    private BrokerManageService brokerManageService;
    private Authentication authentication;
    private ProtocolManager protocolManager;
    private BrokerServer brokerServer;
    private ClusterManager clusterManager;
    private Produce produce;
    private Consume consume;
    private StoreService storeService;
    private ElectionService electionService;
    private MessageRetry retryManager;
    private BrokerContext brokerContext;
    private ContextManager contextManager;
    private StoreManager storeManager;
    private NameService nameService;

    private CoordinatorService coordinatorService;
    private ArchiveManager archiveManager;
    private String[] args;

    @Override
    protected void validate() throws Exception {
        TempMetadataInitializer.prepare();


        this.brokerContext = new BrokerContext();
        Configuration configuration = new Configuration();
        parseParams(configuration, args);

        ContextManager contextManager = new ContextManager(configuration);
        brokerContext.propertySupplier(configuration);

        //start name service first
        this.nameService = getNameService(brokerContext, configuration);
        this.nameService.start();
        this.nameService.addListener(contextManager);
        brokerContext.nameService(nameService);

        // build and start context manager
        this.contextManager.setConfigProvider(new BrokerServiceTest.ConfigProviderImpl(nameService));
        this.contextManager.start();

        //build broker config
        BrokerConfig brokerConfig = new BrokerConfig(configuration);

        //build and cluster manager
        this.clusterManager = new ClusterManager(brokerConfig, nameService, brokerContext);
        this.clusterManager.start();
        brokerContext.clusterManager(this.clusterManager);

        // build store service
        this.storeService = getStoreService(brokerContext);
        brokerContext.storeService(this.storeService);

        // build session manager
        this.sessionManager = new SessionManager();
        brokerContext.sessionManager(this.sessionManager);

        // build authentication
        this.authentication = getAuthentication(brokerContext);
        brokerContext.authentication(this.authentication);

        // new AppTokenAuthentication(clusterManager, brokerConfig.getJmqAdmin());
        this.brokerMonitorService = new BrokerMonitorService(clusterManager.getBrokerId(),
                new BrokerMonitorConfig(configuration, brokerConfig),
                sessionManager);
        brokerContext.brokerManageService(this.brokerManageService);

        // new coordinator service
        this.coordinatorService = new CoordinatorService(new CoordinatorConfig(configuration),
                clusterManager, nameService);
        brokerContext.coordinnatorService(this.coordinatorService);

        // build produce
        this.produce = getProduce(brokerContext);
        this.brokerContext.produce(produce);

        // build message retry
        this.retryManager = getMessageRetry(brokerContext);
        brokerContext.retryManager(retryManager);

        // build consume
        this.archiveManager = new ArchiveManager(brokerContext);
        brokerContext.archiveManager(archiveManager);

        // build consume
        this.consume = getConsume(brokerContext);
        this.brokerContext.consume(consume);

        // build election
        this.electionService = getElectionService(brokerContext);
        this.brokerContext.electionService(electionService);

        // manage service
        this.brokerManageService = new BrokerManageService(new BrokerManageConfig(configuration,brokerConfig),
                brokerMonitorService,
                clusterManager,
                storeService.getManageService(),
                storeService,
                consume,
                retryManager,
                coordinatorService,
                archiveManager,
                nameService,
                storeService,
                electionService);
        this.brokerContext.brokerManageService(brokerManageService);

        //build store manager
        this.storeManager = new StoreManager(storeService, nameService, clusterManager, electionService);
        //build protocol manager
        this.protocolManager = new ProtocolManager(brokerContext);
        //build broker server
        this.brokerServer = new BrokerServer(brokerContext, protocolManager);


        short[] partitions = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        String topic = "jmq@test";
        if (!storeService.partitionGroupExists(topic, 1)) {
            storeService.createPartitionGroup(topic, 1, partitions, new int[]{0});
        } else {
            storeService.restorePartitionGroup(topic, 1);
        }
    }


    @Override
    protected void doStart() throws Exception {
        startIfNecessary(this.nameService);
        startIfNecessary(contextManager);
        startIfNecessary(clusterManager);
        startIfNecessary(storeService);
        startIfNecessary(sessionManager);
        startIfNecessary(retryManager);
        startIfNecessary(brokerMonitorService);
        startIfNecessary(consume);
        startIfNecessary(storeService);
        //must start after store manager
        startIfNecessary(electionService);
        startIfNecessary(protocolManager);
        startIfNecessary(brokerServer);
        startIfNecessary(coordinatorService);
        startIfNecessary(brokerManageService);
        logger.info("brokerServer start ,broker.id[{}],ip[{}],frontPort[{}],backendPort[{}],monitorPort[{}],nameServer port[{}]",
                brokerConfig.getBrokerId(),
                clusterManager.getBroker().getIp(),
                brokerConfig.getFrontendConfig().getPort(),
                brokerConfig.getBackendConfig().getPort(),
                brokerConfig.getBroker().getMonitorPort(),
                brokerConfig.getBroker().getManagerPort());
    }


    public void parseParams(Configuration configuration, String[] args) {
        //TODO 解析参数

    }

    private NameService getNameService(BrokerContext brokerContext, Configuration configuration) {
        Property property = configuration.getProperty(NAMESERVICE_NAME);
        NameService nameService = Plugins.NAMESERVICE.get(property == null ? null : property.getString());
        Preconditions.checkArgument(nameService == null, "nameService not found!");
        enrichIfNecessary(nameService, brokerContext);
        return nameService;
    }


    private StoreService getStoreService(BrokerContext brokerContext) {
        StoreService storeService = Plugins.STORE.get();
        Preconditions.checkArgument(storeService == null, "store service not found!");
        enrichIfNecessary(storeService, brokerContext);
        return storeService;
    }

    private Authentication getAuthentication(BrokerContext brokerContext) {
        Authentication authentication = Plugins.AUTHENTICATION.get();
        Preconditions.checkArgument(authentication == null, "authentication can  not be null");
        enrichIfNecessary(authentication, brokerContext);
        return authentication;
    }

    private Produce getProduce(BrokerContext brokerContext) {
        Produce produce = Plugins.PRODUCE.get();
        Preconditions.checkArgument(produce == null, "produce can not be null");
        enrichIfNecessary(produce, brokerContext);
        return produce;
    }

    private MessageRetry getMessageRetry(BrokerContext brokerContext) {
        //TODO 由于要动态调整重试方式，直接new 一个默认实现
        MessageRetry messageRetry = new BrokerRetryManager(brokerContext);
        return messageRetry;
    }

    private Consume getConsume(BrokerContext brokerContext) {
        Consume consume = Plugins.CONSUME.get();
        Preconditions.checkArgument(consume == null, "consume can not be null");
        enrichIfNecessary(consume, brokerContext);
        return consume;
    }

    private ElectionService getElectionService(BrokerContext brokerContext) {
        ElectionService electionService = Plugins.ELECTION.get();
        Preconditions.checkArgument(electionService == null, "election service can not be null");
        enrichIfNecessary(electionService, brokerContext);
        return electionService;
    }


    public void enrichIfNecessary(Object obj, BrokerContext brokerContext) {
        if (obj == null) {
            return;
        }
        if (obj instanceof PropertySupplierAware) {
            ((PropertySupplierAware) obj).setSupplier(brokerContext.getPropertySupplier());
        }

        if (obj instanceof BrokerContextAware) {
            ((BrokerContextAware) obj).setBrokerContext(brokerContext);
        }
    }

    private void startIfNecessary(Object object) throws Exception {
        if (object instanceof LifeCycle) {
            ((LifeCycle) object).start();
        }
    }

    private void destroy(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof LifeCycle) {
            Close.close((LifeCycle) object);
        }

        if (object instanceof Closeable) {
            Close.close((Closeable) object);
        }
    }


    public BrokerContext getBrokerContext() {
        return brokerContext;
    }


    private class ConfigProviderImpl implements ContextManager.ConfigProvider {
        private NameService nameService;

        public ConfigProviderImpl(NameService nameService) {
            this.nameService = nameService;
        }

        @Override
        public List<Config> getConfigs() {
            return nameService.getAllConfigs();
        }

        @Override
        public String getConfig(String group, String key) {
            return nameService.getConfig(group, key);
        }}

}
