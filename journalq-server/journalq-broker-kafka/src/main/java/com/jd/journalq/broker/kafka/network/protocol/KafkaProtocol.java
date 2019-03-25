package com.jd.journalq.broker.kafka.network.protocol;

import com.jd.journalq.broker.kafka.session.KafkaConnectionHandler;
import com.jd.journalq.broker.kafka.session.KafkaConnectionManager;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.coordinator.CoordinatorGroupManager;
import com.jd.journalq.broker.kafka.KafkaConsts;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.handler.ratelimit.KafkaRateLimitHandlerFactory;
import com.jd.journalq.broker.kafka.manage.KafkaManageServiceFactory;
import com.jd.journalq.broker.kafka.util.RateLimiter;
import com.jd.journalq.common.network.protocol.ChannelHandlerProvider;
import com.jd.journalq.common.network.protocol.ProtocolService;
import com.jd.journalq.common.network.transport.codec.CodecFactory;
import com.jd.journalq.common.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.broker.kafka.coordinator.GroupBalanceHandler;
import com.jd.journalq.broker.kafka.coordinator.GroupBalanceManager;
import com.jd.journalq.broker.kafka.coordinator.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.GroupOffsetHandler;
import com.jd.journalq.broker.kafka.coordinator.GroupOffsetManager;
import com.jd.journalq.broker.kafka.coordinator.KafkaCoordinator;
import com.jd.journalq.broker.kafka.coordinator.KafkaCoordinatorGroupManager;
import com.jd.journalq.broker.kafka.network.helper.KafkaProtocolHelper;
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
public class KafkaProtocol extends Service implements ProtocolService, BrokerContextAware, ChannelHandlerProvider {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaProtocol.class);

    private KafkaConfig config;
    private KafkaCoordinator coordinator;
    private KafkaCoordinatorGroupManager groupMetadataManager;
    private GroupOffsetManager groupOffsetManager;
    private GroupBalanceManager groupBalanceManager;
    private GroupOffsetHandler groupOffsetHandler;
    private GroupBalanceHandler groupBalanceHandler;
    private GroupCoordinator groupCoordinator;
    private KafkaConnectionManager connectionManager;
    private KafkaRateLimitHandlerFactory rateLimitHandlerFactory;
    private KafkaConnectionHandler connectionHandler;
    private KafkaContext kafkaContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.config = new KafkaConfig(brokerContext.getPropertySupplier());
        CoordinatorGroupManager coordinatorGroupManager = brokerContext.getCoordinatorService().getOrCreateCoordinatorGroupManager(KafkaConsts.COORDINATOR_NAMESPACE);

        this.groupMetadataManager = new KafkaCoordinatorGroupManager(config, coordinatorGroupManager);
        this.groupOffsetManager = new GroupOffsetManager(config, brokerContext.getClusterManager());
        this.groupBalanceManager = new GroupBalanceManager(config, groupMetadataManager);

        this.coordinator = new KafkaCoordinator(brokerContext.getCoordinatorService().getCoordinator());
        this.groupOffsetHandler = new GroupOffsetHandler(config, this.coordinator, groupMetadataManager, groupBalanceManager, groupOffsetManager);
        this.groupBalanceHandler = new GroupBalanceHandler(config, groupMetadataManager, groupBalanceManager);
        this.groupCoordinator = new GroupCoordinator(this.coordinator, groupBalanceHandler, groupOffsetHandler);

        this.connectionManager = new KafkaConnectionManager(brokerContext.getSessionManager());

        this.rateLimitHandlerFactory = newRateLimitKafkaHandlerFactory(config);
        this.connectionHandler = new KafkaConnectionHandler(connectionManager);

        this.kafkaContext = new KafkaContext(config, connectionManager, groupMetadataManager,
                groupOffsetManager, groupBalanceManager, groupOffsetHandler, groupBalanceHandler, groupCoordinator, rateLimitHandlerFactory, brokerContext);

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
        rateLimitHandlerFactory.start();
    }

    @Override
    protected void doStop() {
        groupCoordinator.stop();
        groupOffsetManager.stop();
        groupBalanceManager.stop();
        groupOffsetHandler.stop();
        groupBalanceHandler.stop();
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
    public String type() {
        return KafkaConsts.PROTOCOL_TYPE;
    }
}