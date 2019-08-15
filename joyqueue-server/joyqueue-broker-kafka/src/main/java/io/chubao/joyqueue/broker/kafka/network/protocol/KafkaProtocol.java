/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.kafka.network.protocol;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.kafka.KafkaConsts;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.Coordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupBalanceHandler;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupBalanceManager;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupMetadataManager;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupOffsetHandler;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupOffsetManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.ProducerIdManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionHandler;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionMetadataManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionOffsetHandler;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.ProducerSequenceManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.completion.TransactionCompletionHandler;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.completion.TransactionCompletionScheduler;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.log.TransactionLog;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.synchronizer.TransactionSynchronizer;
import io.chubao.joyqueue.broker.kafka.manage.KafkaManageServiceFactory;
import io.chubao.joyqueue.broker.kafka.network.helper.KafkaProtocolHelper;
import io.chubao.joyqueue.broker.kafka.session.KafkaConnectionHandler;
import io.chubao.joyqueue.broker.kafka.session.KafkaConnectionManager;
import io.chubao.joyqueue.broker.kafka.session.KafkaTransportHandler;
import io.chubao.joyqueue.network.protocol.CommandHandlerProvider;
import io.chubao.joyqueue.network.protocol.ExceptionHandlerProvider;
import io.chubao.joyqueue.network.protocol.ProtocolService;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.toolkit.service.Service;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka协议
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class KafkaProtocol extends Service implements ProtocolService, BrokerContextAware, CommandHandlerProvider, ExceptionHandlerProvider {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaProtocol.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private GroupMetadataManager groupMetadataManager;
    private GroupOffsetManager groupOffsetManager;
    private GroupBalanceManager groupBalanceManager;
    private GroupOffsetHandler groupOffsetHandler;
    private GroupBalanceHandler groupBalanceHandler;
    private GroupCoordinator groupCoordinator;

    private ProducerIdManager producerIdManager;
    private TransactionIdManager transactionIdManager;
    private ProducerSequenceManager producerSequenceManager;
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;
    private TransactionCompletionHandler transactionCompletionHandler;
    private TransactionCompletionScheduler transactionCompletionScheduler;
    private TransactionHandler transactionHandler;
    private TransactionOffsetHandler transactionOffsetHandler;
    private TransactionCoordinator transactionCoordinator;
    private KafkaConnectionManager connectionManager;

    private KafkaConnectionHandler connectionHandler;
    private KafkaTransportHandler transportHandler;
    private KafkaContext kafkaContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        io.chubao.joyqueue.broker.coordinator.group.GroupMetadataManager groupMetadataManager =
                brokerContext.getCoordinatorService().getOrCreateGroupMetadataManager(KafkaConsts.COORDINATOR_NAMESPACE);
        io.chubao.joyqueue.broker.coordinator.transaction.TransactionMetadataManager transactionMetadataManager =
                brokerContext.getCoordinatorService().getOrCreateTransactionMetadataManager(KafkaConsts.COORDINATOR_NAMESPACE);

        this.config = new KafkaConfig(brokerContext.getPropertySupplier());
        this.coordinator = new Coordinator(brokerContext.getCoordinatorService().getCoordinator());

        this.groupMetadataManager = new GroupMetadataManager(config, groupMetadataManager);
        this.groupOffsetManager = new GroupOffsetManager(config, brokerContext.getClusterManager(), this.groupMetadataManager, coordinator.getSessionManager());
        this.groupBalanceManager = new GroupBalanceManager(config, this.groupMetadataManager);
        this.groupOffsetHandler = new GroupOffsetHandler(config, coordinator, this.groupMetadataManager, groupBalanceManager, groupOffsetManager);
        this.groupBalanceHandler = new GroupBalanceHandler(config, this.groupMetadataManager, groupBalanceManager);
        this.groupCoordinator = new GroupCoordinator(coordinator, groupBalanceHandler, groupOffsetHandler, this.groupMetadataManager);

        this.producerIdManager = new ProducerIdManager();
        this.transactionIdManager = new TransactionIdManager();
        this.producerSequenceManager = new ProducerSequenceManager(config);
        this.transactionMetadataManager = new TransactionMetadataManager(config, transactionMetadataManager);
        this.transactionLog = new TransactionLog(config, brokerContext.getProduce(), brokerContext.getConsume(), coordinator, brokerContext.getClusterManager());
        this.transactionSynchronizer = new TransactionSynchronizer(config, transactionIdManager, transactionLog, coordinator.getSessionManager(), brokerContext.getNameService());
        this.transactionCompletionHandler = new TransactionCompletionHandler(config, coordinator, this.transactionMetadataManager, transactionLog, transactionSynchronizer);
        this.transactionCompletionScheduler = new TransactionCompletionScheduler(config, transactionCompletionHandler);
        this.transactionHandler = new TransactionHandler(coordinator, this.transactionMetadataManager, producerIdManager, transactionSynchronizer, brokerContext.getNameService());
        this.transactionOffsetHandler = new TransactionOffsetHandler(coordinator, this.transactionMetadataManager, transactionSynchronizer);
        this.transactionCoordinator = new TransactionCoordinator(coordinator, this.transactionMetadataManager, transactionHandler, transactionOffsetHandler);

        this.connectionManager = new KafkaConnectionManager(brokerContext.getSessionManager());

        this.connectionHandler = new KafkaConnectionHandler(connectionManager);
        this.transportHandler = new KafkaTransportHandler(config);

        this.kafkaContext = new KafkaContext(config, groupCoordinator, transactionCoordinator, transactionIdManager, producerSequenceManager, brokerContext);
        registerManage(brokerContext, kafkaContext);
    }

    protected void registerManage(BrokerContext brokerContext, KafkaContext kafkaContext) {
        KafkaManageServiceFactory manageServiceFactory = new KafkaManageServiceFactory(brokerContext, kafkaContext);
        brokerContext.getBrokerManageService().registerService("kafkaManageService", manageServiceFactory.getKafkaManageService());
        brokerContext.getBrokerManageService().registerService("kafkaMonitorService", manageServiceFactory.getKafkaMonitorService());
    }

    @Override
    public void doStart() throws Exception {
        groupOffsetManager.start();
        groupBalanceManager.start();
        groupOffsetHandler.start();
        groupBalanceHandler.start();
        groupCoordinator.start();

        transactionCoordinator.start();
        transactionLog.start();
        transactionSynchronizer.start();
        transactionHandler.start();
        transactionOffsetHandler.start();
        transactionCompletionHandler.start();
        transactionCompletionScheduler.start();
    }

    @Override
    protected void doStop() {
        groupCoordinator.stop();
        groupOffsetManager.stop();
        groupBalanceManager.stop();
        groupOffsetHandler.stop();
        groupBalanceHandler.stop();

        transactionCompletionScheduler.stop();
        transactionCompletionHandler.stop();
        transactionOffsetHandler.stop();
        transactionHandler.stop();
        transactionSynchronizer.stop();
        transactionLog.stop();
        transactionCoordinator.stop();
    }

    @Override
    public boolean isSupport(ByteBuf buffer) {
        return KafkaProtocolHelper.isSupport(buffer);
    }

    @Override
    public CodecFactory createCodecFactory() {
        return new KafkaCodecFactory();
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        return new KafkaCommandHandlerFactory(kafkaContext);
    }

    @Override
    public ChannelHandler getCommandHandler(ChannelHandler channelHandler) {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast(transportHandler)
                        .addLast(connectionHandler)
                        .addLast(channelHandler);
            }
        };
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new KafkaExceptionHandler();
    }

    @Override
    public String type() {
        return KafkaConsts.PROTOCOL_TYPE;
    }
}