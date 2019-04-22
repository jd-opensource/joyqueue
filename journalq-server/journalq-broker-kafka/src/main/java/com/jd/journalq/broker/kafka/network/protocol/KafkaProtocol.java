package com.jd.journalq.broker.kafka.network.protocol;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.kafka.KafkaConsts;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.group.GroupBalanceHandler;
import com.jd.journalq.broker.kafka.coordinator.group.GroupBalanceManager;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.group.GroupMetadataManager;
import com.jd.journalq.broker.kafka.coordinator.group.GroupOffsetHandler;
import com.jd.journalq.broker.kafka.coordinator.group.GroupOffsetManager;
import com.jd.journalq.broker.kafka.coordinator.transaction.ProducerIdManager;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionHandler;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionIdManager;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionLog;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionMetadataManager;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionOffsetHandler;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionSynchronizer;
import com.jd.journalq.broker.kafka.coordinator.transaction.completion.TransactionCompletionHandler;
import com.jd.journalq.broker.kafka.coordinator.transaction.completion.TransactionCompletionScheduler;
import com.jd.journalq.broker.kafka.handler.ratelimit.KafkaRateLimitHandlerFactory;
import com.jd.journalq.broker.kafka.manage.KafkaManageServiceFactory;
import com.jd.journalq.broker.kafka.network.helper.KafkaProtocolHelper;
import com.jd.journalq.broker.kafka.session.KafkaConnectionHandler;
import com.jd.journalq.broker.kafka.session.KafkaConnectionManager;
import com.jd.journalq.broker.kafka.util.RateLimiter;
import com.jd.journalq.network.protocol.ChannelHandlerProvider;
import com.jd.journalq.network.protocol.ExceptionHandlerProvider;
import com.jd.journalq.network.protocol.ProtocolService;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.toolkit.delay.DelayedOperationManager;
import com.jd.journalq.toolkit.service.Service;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka协议
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class KafkaProtocol extends Service implements ProtocolService, BrokerContextAware, ChannelHandlerProvider, ExceptionHandlerProvider {

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
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;
    private TransactionCompletionHandler transactionCompletionHandler;
    private TransactionCompletionScheduler transactionCompletionScheduler;
    private TransactionHandler transactionHandler;
    private TransactionOffsetHandler transactionOffsetHandler;
    private TransactionCoordinator transactionCoordinator;
    private KafkaConnectionManager connectionManager;

    private KafkaRateLimitHandlerFactory rateLimitHandlerFactory;
    private KafkaConnectionHandler connectionHandler;
    private KafkaContext kafkaContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        com.jd.journalq.broker.coordinator.group.GroupMetadataManager groupMetadataManager = brokerContext.getCoordinatorService().getOrCreateGroupMetadataManager(KafkaConsts.COORDINATOR_NAMESPACE);
        com.jd.journalq.broker.coordinator.transaction.TransactionMetadataManager transactionMetadataManager = brokerContext.getCoordinatorService().getOrCreateTransactionMetadataManager(KafkaConsts.COORDINATOR_NAMESPACE);

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
        this.transactionMetadataManager = new TransactionMetadataManager(config, transactionMetadataManager);
        this.transactionLog = new TransactionLog(config, brokerContext.getProduce(), brokerContext.getConsume(), coordinator, brokerContext.getClusterManager());
        this.transactionSynchronizer = new TransactionSynchronizer(config, transactionIdManager, transactionLog, coordinator.getSessionManager(), brokerContext.getNameService());
        this.transactionCompletionHandler = new TransactionCompletionHandler(config, coordinator, transactionLog, transactionSynchronizer);
        this.transactionCompletionScheduler = new TransactionCompletionScheduler(config, transactionCompletionHandler);
        this.transactionHandler = new TransactionHandler(coordinator, this.transactionMetadataManager, producerIdManager, transactionSynchronizer, brokerContext.getNameService());
        this.transactionOffsetHandler = new TransactionOffsetHandler(coordinator, this.transactionMetadataManager, transactionSynchronizer);
        this.transactionCoordinator = new TransactionCoordinator(coordinator, this.transactionMetadataManager, transactionHandler, transactionOffsetHandler);

        this.connectionManager = new KafkaConnectionManager(brokerContext.getSessionManager());
        this.rateLimitHandlerFactory = newRateLimitKafkaHandlerFactory(config);
        this.connectionHandler = new KafkaConnectionHandler(connectionManager);

        this.kafkaContext = new KafkaContext(config, groupCoordinator, transactionCoordinator, transactionIdManager, rateLimitHandlerFactory, brokerContext);
        registerManage(brokerContext, kafkaContext);
    }

    protected KafkaRateLimitHandlerFactory newRateLimitKafkaHandlerFactory(KafkaConfig config) {
        DelayedOperationManager rateLimitDelayedOperation = new DelayedOperationManager("kafkaRateLimit");
        RateLimiter rateLimiter = new RateLimiter(config);
        return new KafkaRateLimitHandlerFactory(config, rateLimitDelayedOperation, rateLimiter);
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
        transactionCompletionScheduler.start();
        rateLimitHandlerFactory.start();
    }

    @Override
    protected void doStop() {
        groupCoordinator.stop();
        groupOffsetManager.stop();
        groupBalanceManager.stop();
        groupOffsetHandler.stop();
        groupBalanceHandler.stop();

        transactionCompletionScheduler.stop();
        transactionOffsetHandler.stop();
        transactionHandler.stop();
        transactionSynchronizer.stop();
        transactionLog.stop();
        transactionCoordinator.stop();
        rateLimitHandlerFactory.stop();
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
    public ChannelHandler getChannelHandler(ChannelHandler channelHandler) {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(channelHandler).addLast(connectionHandler);
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