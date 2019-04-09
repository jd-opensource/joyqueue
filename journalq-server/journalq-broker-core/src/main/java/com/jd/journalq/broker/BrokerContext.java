package com.jd.journalq.broker;

import com.jd.journalq.broker.archive.ArchiveManager;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.MessageConvertSupport;
import com.jd.journalq.broker.consumer.position.PositionManager;
import com.jd.journalq.broker.coordinator.CoordinatorService;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.manage.BrokerManageService;
import com.jd.journalq.broker.monitor.BrokerMonitor;
import com.jd.journalq.broker.monitor.BrokerMonitorService;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.security.Authentication;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * broker上下文
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class BrokerContext {
    private BrokerConfig brokerConfig;
    private SessionManager sessionManager;
    private ClusterManager clusterManager;
    private Produce produce;
    private Consume consume;
    private PositionManager positionManager;
    private Authentication authentication;
    private StoreService storeService;
    private ElectionService electionService;
    private MessageRetry retryManager;
    private BrokerMonitorService brokerMonitorService;
    private BrokerManageService brokerManageService;
    private NameService nameService;
    private CoordinatorService coordinatorService;
    private PropertySupplier propertySupplier;
    private ArchiveManager archiveManager;
    private Consumer.ConsumerPolicy globalConsumerPolicy;
    private Producer.ProducerPolicy globalproducerPolicy;
    private MessageConvertSupport messageConvertSupport;

    public BrokerContext() {
    }


    @Deprecated
    public BrokerContext(BrokerConfig brokerConfig, SessionManager sessionManager, ClusterManager clusterManager,
                         Produce produce, Consume consume, Authentication authentication, StoreService storeService,
                         ElectionService electionService, MessageRetry retryManager, BrokerMonitorService brokerMonitorService,
                         BrokerManageService brokerManageService, NameService nameService, CoordinatorService coordinatorService) {

        this.brokerConfig = brokerConfig;
        this.sessionManager = sessionManager;
        this.clusterManager = clusterManager;
        this.produce = produce;
        this.consume = consume;
        this.authentication = authentication;
        this.storeService = storeService;
        this.electionService = electionService;
        this.retryManager = retryManager;
        this.brokerMonitorService = brokerMonitorService;
        this.brokerManageService = brokerManageService;
        this.nameService = nameService;
        this.coordinatorService = coordinatorService;
    }

    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    public BrokerMonitor getBrokerMonitor() {
        return brokerMonitorService.getBrokerMonitor();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public Produce getProduce() {
        return produce;
    }

    public Consume getConsume() {
        return consume;
    }

    public PositionManager getPositionManager() {
        return positionManager;
    }

    public NameService getNameService() {
        return nameService;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public ElectionService getElectionService() {
        return electionService;
    }

    public MessageRetry getRetryManager() {
        return retryManager;
    }

    public BrokerMonitorService getBrokerMonitorService() {
        return brokerMonitorService;
    }

    public BrokerManageService getBrokerManageService() {
        return brokerManageService;
    }

    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }


    public CoordinatorService getCoordinatorService() {
        return coordinatorService;
    }

    public Consumer.ConsumerPolicy getConsumerPolicy() {
        return globalConsumerPolicy;
    }

    public Producer.ProducerPolicy getProducerPolicy() {
        return globalproducerPolicy;
    }

    public MessageConvertSupport getMessageConvertSupport() {
        return messageConvertSupport;
    }

    public PropertySupplier getPropertySupplier() {

        return propertySupplier;
    }

    public ArchiveManager getArchiveManager() {
        return archiveManager;
    }

    public BrokerContext brokerConfig(BrokerConfig config) {
        this.brokerConfig = config;
        return this;
    }

    public BrokerContext brokerMonitorService(BrokerMonitorService brokerMonitorService) {
        this.brokerMonitorService = brokerMonitorService;
        return this;
    }

    public BrokerContext sessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        return this;
    }

    public BrokerContext clusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        return this;
    }

    public BrokerContext consume(Consume consume) {
        this.consume = consume;
        return this;
    }

    public BrokerContext produce(Produce produce) {
        this.produce = produce;
        return this;
    }

    public BrokerContext positionManager(PositionManager positionManager) {
        this.positionManager = positionManager;
        return this;
    }

    public BrokerContext authentication(Authentication authentication) {
        this.authentication = authentication;
        return this;
    }

    public BrokerContext storeService(StoreService storeService) {
        this.storeService = storeService;
        return this;
    }

    public BrokerContext electionService(ElectionService electionService) {
        this.electionService = electionService;
        return this;
    }


    public BrokerContext retryManager(MessageRetry messageRetry) {
        this.retryManager = messageRetry;
        return this;
    }

    public BrokerContext brokerManageService(BrokerManageService brokerManageService) {
        this.brokerManageService = brokerManageService;
        return this;
    }

    public BrokerContext nameService(NameService nameService) {
        this.nameService = nameService;
        return this;
    }

    public BrokerContext coordinnatorService(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
        return this;
    }

    public BrokerContext propertySupplier(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        return this;
    }

    public BrokerContext archiveManager(ArchiveManager archiveManager) {
        this.archiveManager = archiveManager;
        return this;
    }

    public BrokerContext producerPolicy(Producer.ProducerPolicy producerPolicy) {
        this.globalproducerPolicy = producerPolicy;
        return this;
    }

    public BrokerContext consumerPolicy(Consumer.ConsumerPolicy consumerPolicy) {
        this.globalConsumerPolicy = consumerPolicy;
        return this;
    }

    public BrokerContext messageConvertSupport(MessageConvertSupport messageConvertSupport) {
        this.messageConvertSupport = messageConvertSupport;
        return this;
    }

    public Broker getBroker() {
        return null==clusterManager?null:clusterManager.getBroker();
    }
}