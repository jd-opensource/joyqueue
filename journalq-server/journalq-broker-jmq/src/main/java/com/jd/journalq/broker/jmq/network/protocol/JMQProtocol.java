package com.jd.journalq.broker.jmq.network.protocol;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.jmq.JMQConsts;
import com.jd.journalq.broker.jmq.JMQContext;
import com.jd.journalq.broker.jmq.config.JMQConfig;
import com.jd.journalq.broker.jmq.coordinator.GroupMetadataManager;
import com.jd.journalq.broker.jmq.coordinator.JMQCoordinator;
import com.jd.journalq.broker.jmq.coordinator.assignment.PartitionAssignmentHandler;
import com.jd.journalq.broker.jmq.network.protocol.helper.JMQProtocolHelper;
import com.jd.journalq.broker.polling.LongPollingManager;
import com.jd.journalq.network.codec.JMQCodecFactory;
import com.jd.journalq.network.protocol.ExceptionHandlerProvider;
import com.jd.journalq.network.protocol.ProtocolService;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.toolkit.service.Service;
import io.netty.buffer.ByteBuf;

/**
 * JMQProtocol
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/27
 */
public class JMQProtocol extends Service implements ProtocolService, BrokerContextAware, ExceptionHandlerProvider {

    private JMQConfig config;
    private JMQCoordinator coordinator;
    private GroupMetadataManager coordinatorGroupManager;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private LongPollingManager longPollingManager;
    private JMQContext jmqContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.config = new JMQConfig(brokerContext.getPropertySupplier());

        this.coordinator = new JMQCoordinator(brokerContext.getCoordinatorService().getCoordinator());
        this.coordinatorGroupManager = new GroupMetadataManager(config, brokerContext.getCoordinatorService().getOrCreateGroupMetadataManager(JMQConsts.COORDINATOR_NAMESPACE));
        this.partitionAssignmentHandler = new PartitionAssignmentHandler(config, coordinatorGroupManager);
        this.longPollingManager = new LongPollingManager(brokerContext.getSessionManager(), brokerContext.getClusterManager(), brokerContext.getConsume(),brokerContext.getPropertySupplier());

        this.jmqContext = new JMQContext(config, coordinator, coordinatorGroupManager, partitionAssignmentHandler, longPollingManager, brokerContext);
    }

    @Override
    protected void doStart() throws Exception {
        partitionAssignmentHandler.start();
        longPollingManager.start();
    }

    @Override
    protected void doStop() {
        partitionAssignmentHandler.stop();
        longPollingManager.stop();
    }

    @Override
    public boolean isSupport(ByteBuf buffer) {
        return JMQProtocolHelper.isSupport(buffer);
    }

    @Override
    public CodecFactory createCodecFactory() {
        return new JMQCodecFactory();
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        return new JMQCommandHandlerFactory(jmqContext);
    }

    @Override
    public String type() {
        return JMQConsts.PROTOCOL_TYPE;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new JMQExceptionHandler();
    }
}