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
package io.chubao.joyqueue.broker;

import com.google.common.base.Preconditions;
import io.chubao.joyqueue.broker.archive.ArchiveManager;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.config.BrokerConfig;
import io.chubao.joyqueue.broker.config.BrokerStoreConfig;
import io.chubao.joyqueue.broker.config.Configuration;
import io.chubao.joyqueue.broker.config.ConfigurationManager;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.consumer.MessageConvertSupport;
import io.chubao.joyqueue.broker.coordinator.CoordinatorService;
import io.chubao.joyqueue.broker.coordinator.config.CoordinatorConfig;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.helper.AwareHelper;
import io.chubao.joyqueue.broker.manage.BrokerManageService;
import io.chubao.joyqueue.broker.manage.config.BrokerManageConfig;
import io.chubao.joyqueue.broker.manage.config.BrokerManageConfigKey;
import io.chubao.joyqueue.broker.monitor.BrokerMonitorService;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.broker.monitor.config.BrokerMonitorConfig;
import io.chubao.joyqueue.broker.network.BrokerServer;
import io.chubao.joyqueue.broker.network.protocol.ProtocolManager;
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.broker.retry.BrokerRetryManager;
import io.chubao.joyqueue.broker.store.StoreInitializer;
import io.chubao.joyqueue.broker.store.StoreManager;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.network.transport.config.ServerConfig;
import io.chubao.joyqueue.network.transport.config.TransportConfigSupport;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.config.NameServerConfigKey;
import io.chubao.joyqueue.nsr.nameservice.CompensatedNameService;
import io.chubao.joyqueue.security.Authentication;
import io.chubao.joyqueue.server.retry.api.MessageRetry;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.lang.Close;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * BrokerService
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class BrokerService extends Service {
    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private static final String NAMESERVICE_NAME = "nameserver.nsr.name";
    private static final String DEFAULT_NAMESERVICE_NAME = "server";
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
    private StoreInitializer storeInitializer;
    private ElectionService electionService;
    private MessageRetry retryManager;
    private BrokerContext brokerContext;
    private ConfigurationManager configurationManager;
    private StoreManager storeManager;
    private NameService nameService;

    private CoordinatorService coordinatorService;
    private ArchiveManager archiveManager;
    private MessageConvertSupport messageConvertSupport;
    private String[] args;

    public BrokerService() {
    }

    public BrokerService(String[] args) {
        this.args = args;
    }

    @Override
    protected void validate() throws Exception {
        this.brokerContext = new BrokerContext();
        this.configurationManager = new ConfigurationManager(args);
        configurationManager.start();
        Configuration configuration = configurationManager.getConfiguration();
        enrichServicePorts(configuration);
        brokerContext.propertySupplier(configuration);

        //build broker config
        this.brokerConfig = new BrokerConfig(configuration);
        String dataPath = brokerConfig.getAndCreateDataPath();
        logger.info("Broker data path: {}.", dataPath);

        this.brokerContext.brokerConfig(brokerConfig);

        //start name service first
        this.nameService = getNameService(brokerContext, configuration);
        this.nameService.start();
        this.brokerContext.nameService(nameService);

        // build and start context manager
        this.nameService.addListener(configurationManager);
        this.configurationManager.setConfigProvider(new ConfigProviderImpl(nameService));


        //build and cluster manager
        this.clusterManager = new ClusterManager(brokerConfig, nameService, brokerContext);
        this.clusterManager.start();
        this.brokerContext.clusterManager(this.clusterManager);

        // build store service
        this.storeService = getStoreService(brokerContext);
        this.brokerContext.storeService(this.storeService);

        // build session manager
        this.sessionManager = new SessionManager();
        this.brokerContext.sessionManager(this.sessionManager);

        // build authentication
        this.authentication = getAuthentication(brokerContext);
        this.brokerContext.authentication(this.authentication);

        // new AppTokenAuthentication(clusterManager, brokerConfig.getJmqAdmin());
        this.brokerMonitorService = new BrokerMonitorService(clusterManager.getBrokerId(),
                new BrokerMonitorConfig(configuration, brokerConfig),
                sessionManager,
                clusterManager);
        this.brokerContext.brokerMonitorService(this.brokerMonitorService);

        // new coordinator service
        this.coordinatorService = new CoordinatorService(new CoordinatorConfig(configuration),
                clusterManager, nameService);
        this.brokerContext.coordinnatorService(this.coordinatorService);

        this.messageConvertSupport = new MessageConvertSupport();
        this.brokerContext.messageConvertSupport(this.messageConvertSupport);

        // build produce
        this.produce = getProduce(brokerContext);
        this.brokerContext.produce(produce);

        // build message retry
        this.retryManager = getMessageRetry(brokerContext);
        if(null != this.retryManager) {
            this.retryManager.setSupplier(configuration);
        }
        this.brokerContext.retryManager(retryManager);

        // build consume
        this.archiveManager = new ArchiveManager(brokerContext);
        this.brokerContext.archiveManager(archiveManager);

        // build consume
        this.consume = getConsume(brokerContext);
        this.brokerContext.consume(consume);

        // build election
        this.electionService = getElectionService(brokerContext);
        this.brokerContext.electionService(electionService);

        this.storeInitializer = new StoreInitializer(new BrokerStoreConfig(configuration), nameService,
                clusterManager, storeService, electionService);

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
                electionService,
                messageConvertSupport);
        this.brokerContext.brokerManageService(brokerManageService);

        //build store manager
        this.storeManager = new StoreManager(storeService, nameService, clusterManager, electionService);
        enrichIfNecessary(storeManager, brokerContext);
        //build protocol manager
        this.protocolManager = new ProtocolManager(brokerContext);
        //build broker server
        this.brokerServer = new BrokerServer(brokerContext, protocolManager);
        //build produce policy
        this.brokerContext.producerPolicy(buildGlobalProducePolicy(configuration));
        //build consume policy
        this.brokerContext.consumerPolicy(buildGlobalConsumePolicy(configuration));

    }

    private void enrichServicePorts(Configuration configuration) {
        // broker.frontend-server.transport.server.port	50088	JoyQueue Server与客户端通信的端口
        String key = BrokerConfig.BROKER_FRONTEND_SERVER_CONFIG_PREFIX + TransportConfigSupport.TRANSPORT_SERVER_PORT;
        Property basePortProperty = configuration.getOrCreateProperty(key);
        int port = ServerConfig.DEFAULT_TRANSPORT_PORT;
        try {
                port = basePortProperty.getInteger();
        } catch (NullPointerException | NumberFormatException ignored) {}
        configuration.addProperty(key, String.valueOf(port));

        // broker.backend-server.transport.server.port	50089	内部端口，JoyQueue Server各节点之间通信的端口

        key = BrokerConfig.BROKER_BACKEND_SERVER_CONFIG_PREFIX + TransportConfigSupport.TRANSPORT_SERVER_PORT;
        port += 1;
        configuration.addProperty(key, String.valueOf(port));

        // manager.export.port	50090	Broker监控服务的端口
        key = BrokerManageConfigKey.EXPORT_PORT.getName();
        port += 1;
        configuration.addProperty(key, String.valueOf(port));

        // nameserver.nsr.manage.port	50091	JoyQueue Server rest API 端口

        key = NameServerConfigKey.NAMESERVER_MANAGE_PORT.getName();
        port += 1;
        configuration.addProperty(key, String.valueOf(port));

        // nameserver.transport.server.port	50092	内部端口，JoyQueue Server各节点之间通信的端口。

        key = NameServerConfigKey.NAMESERVER_SERVICE_PORT.getName();
        port += 1;
        configuration.addProperty(key, String.valueOf(port));

    }


    private NameService getNameService(BrokerContext brokerContext, Configuration configuration) {
        Property property = configuration.getProperty(NAMESERVICE_NAME);
        NameService nameService = Plugins.NAMESERVICE.get(property == null ? DEFAULT_NAMESERVICE_NAME : property.getString());
        Preconditions.checkArgument(nameService != null, "nameService not found!");

        CompensatedNameService compensatedNameService = new CompensatedNameService(nameService);
        enrichIfNecessary(nameService, brokerContext);
        enrichIfNecessary(compensatedNameService, brokerContext);
        return compensatedNameService;
    }


    private StoreService getStoreService(BrokerContext brokerContext) {
        StoreService storeService = Plugins.STORE.get();
        Preconditions.checkArgument(storeService != null, "store service not found!");
        enrichIfNecessary(storeService, brokerContext);
        return storeService;
    }

    private Authentication getAuthentication(BrokerContext brokerContext) {
        Authentication authentication = Plugins.AUTHENTICATION.get();
        Preconditions.checkArgument(authentication != null, "authentication can  not be null");
        enrichIfNecessary(authentication, brokerContext);
        return authentication;
    }

    private Produce getProduce(BrokerContext brokerContext) {
        Produce produce = Plugins.PRODUCE.get();
        Preconditions.checkArgument(produce != null, "produce can not be null");
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
        Preconditions.checkArgument(consume != null, "consume can not be null");
        enrichIfNecessary(consume, brokerContext);
        return consume;
    }

    private ElectionService getElectionService(BrokerContext brokerContext) {
        ElectionService electionService = Plugins.ELECTION.get();
        Preconditions.checkArgument(electionService != null, "election service can not be null");
        enrichIfNecessary(electionService, brokerContext);
        return electionService;
    }

    private Consumer.ConsumerPolicy buildGlobalConsumePolicy(PropertySupplier propertySupplier) {
        //TODO
        return new Consumer.ConsumerPolicy.Builder().create();
    }

    private Producer.ProducerPolicy buildGlobalProducePolicy(PropertySupplier propertySupplier) {
        //TODO
        return new Producer.ProducerPolicy.Builder().create();
    }

    @Override
    protected void doStart() throws Exception {
        startIfNecessary(nameService);
        startIfNecessary(clusterManager);
        startIfNecessary(storeService);
        startIfNecessary(storeInitializer);
        startIfNecessary(sessionManager);
        startIfNecessary(retryManager);
        startIfNecessary(brokerMonitorService);
        startIfNecessary(produce);
        startIfNecessary(consume);
        //must start after store manager
        startIfNecessary(storeManager);
        startIfNecessary(electionService);
        startIfNecessary(protocolManager);
        startIfNecessary(brokerServer);
        startIfNecessary(coordinatorService);
        startIfNecessary(brokerManageService);
        startIfNecessary(archiveManager);
        printConfig();
    }


    private void printConfig() {
        StringBuffer buffer = new StringBuffer("broker start with configuration:").append('\n');
        if (configurationManager != null && configurationManager.getConfiguration() != null) {
            List<Property> properties = new ArrayList<>(configurationManager.getConfiguration().getProperties());
            Collections.sort(properties, Comparator.comparing(Property::getKey));
            for (Property property : properties) {
                String value = property.getValue() == null ? "null" : property.getValue().toString();
                buffer.append('\t').append(property.getKey()).append(": ").append(value).append('\n');
            }
        }

        logger.info(buffer.toString());
        logger.info("broker.id[{}],ip[{}],frontPort[{}],backendPort[{}],monitorPort[{}],nameServer port[{}]",
                brokerConfig.getBrokerId(),
                clusterManager.getBroker().getIp(),
                brokerConfig.getFrontendConfig().getPort(),
                brokerConfig.getBackendConfig().getPort(),
                brokerConfig.getBroker().getMonitorPort(),
                brokerConfig.getBroker().getManagerPort());
    }

    @Override
    protected void doStop() {

        destroy(brokerServer);
        destroy(protocolManager);
        destroy(electionService);
        destroy(produce);
        destroy(consume);
        destroy(coordinatorService);
        destroy(sessionManager);
        destroy(clusterManager);
        destroy(storeManager);
        destroy(storeInitializer);
        destroy(storeService);
        destroy(configurationManager);
        destroy(retryManager);
        destroy(archiveManager);
        destroy(brokerMonitorService);
        destroy(brokerManageService);
        destroy(nameService);

        logger.info("Broker stopped!!!!");
    }

    public void enrichIfNecessary(Object obj, BrokerContext brokerContext) {
        AwareHelper.enrichIfNecessary(obj, brokerContext);
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




    private class ConfigProviderImpl implements ConfigurationManager.ConfigProvider {
        private NameService nameService;

        ConfigProviderImpl(NameService nameService) {
            this.nameService = nameService;
        }

        @Override
        public List<Config> getConfigs() {
            return nameService.getAllConfigs();
        }

        @Override
        public String getConfig(String group, String key) {
            return nameService.getConfig(group, key);
        }
    }
}