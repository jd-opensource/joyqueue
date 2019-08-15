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

import io.chubao.joyqueue.broker.archive.ArchiveManager;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.config.BrokerConfig;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.consumer.MessageConvertSupport;
import io.chubao.joyqueue.broker.consumer.position.PositionManager;
import io.chubao.joyqueue.broker.coordinator.CoordinatorService;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.manage.BrokerManageService;
import io.chubao.joyqueue.broker.monitor.BrokerMonitor;
import io.chubao.joyqueue.broker.monitor.BrokerMonitorService;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.security.Authentication;
import io.chubao.joyqueue.server.retry.api.MessageRetry;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;

/**
 * broker上下文
 *
 * author: gaohaoxiang
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