package io.chubao.joyqueue.broker.protocol.network;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.protocol.JoyQueueConsts;
import io.chubao.joyqueue.broker.protocol.JoyQueueContext;
import io.chubao.joyqueue.broker.protocol.config.JoyQueueConfig;
import io.chubao.joyqueue.broker.protocol.coordinator.Coordinator;
import io.chubao.joyqueue.broker.protocol.coordinator.GroupMetadataManager;
import io.chubao.joyqueue.broker.protocol.coordinator.assignment.PartitionAssignmentHandler;
import io.chubao.joyqueue.broker.protocol.network.helper.JoyQueueProtocolHelper;
import io.chubao.joyqueue.broker.polling.LongPollingManager;
import io.chubao.joyqueue.network.protocol.ExceptionHandlerProvider;
import io.chubao.joyqueue.network.protocol.ProtocolService;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.toolkit.service.Service;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueProtocol
 *
 * author: gaohaoxiang
 * date: 2018/11/27
 */
public class JoyQueueProtocol extends Service implements ProtocolService, BrokerContextAware, ExceptionHandlerProvider {

    private JoyQueueConfig config;
    private Coordinator coordinator;
    private GroupMetadataManager coordinatorGroupManager;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private LongPollingManager longPollingManager;
    private JoyQueueContext joyQueueContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.config = new JoyQueueConfig(brokerContext.getPropertySupplier());

        this.coordinator = new Coordinator(brokerContext.getCoordinatorService().getCoordinator());
        this.coordinatorGroupManager = new GroupMetadataManager(config, brokerContext.getCoordinatorService().getOrCreateGroupMetadataManager(JoyQueueConsts.COORDINATOR_NAMESPACE));
        this.partitionAssignmentHandler = new PartitionAssignmentHandler(config, coordinatorGroupManager);
        this.longPollingManager = new LongPollingManager(brokerContext.getSessionManager(), brokerContext.getClusterManager(), brokerContext.getConsume(),brokerContext.getPropertySupplier());

        this.joyQueueContext = new JoyQueueContext(config, coordinator, coordinatorGroupManager, partitionAssignmentHandler, longPollingManager, brokerContext);
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
        return JoyQueueProtocolHelper.isSupport(buffer);
    }

    @Override
    public CodecFactory createCodecFactory() {
        return new JoyQueueCodecFactory();
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        return new JoyQueueCommandHandlerFactory(joyQueueContext);
    }

    @Override
    public String type() {
        return JoyQueueConsts.PROTOCOL_TYPE;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new JoyQueueExceptionHandler();
    }
}