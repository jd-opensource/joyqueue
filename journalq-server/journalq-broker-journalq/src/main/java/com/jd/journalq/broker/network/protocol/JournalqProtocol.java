/**
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
package com.jd.journalq.broker.network.protocol;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.coordinator.JMQCoordinator;
import com.jd.journalq.broker.coordinator.JMQCoordinatorGroupManager;
import com.jd.journalq.broker.JournalqConsts;
import com.jd.journalq.broker.JournalqContext;
import com.jd.journalq.broker.config.JournalqConfig;
import com.jd.journalq.broker.coordinator.assignment.PartitionAssignmentHandler;
import com.jd.journalq.broker.network.protocol.helper.JournalqProtocolHelper;
import com.jd.journalq.broker.polling.LongPollingManager;
import com.jd.journalq.network.codec.JournalqCodecFactory;
import com.jd.journalq.network.protocol.ExceptionHandlerProvider;
import com.jd.journalq.network.protocol.ProtocolService;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.toolkit.service.Service;
import io.netty.buffer.ByteBuf;

/**
 * JournalqProtocol
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/27
 */
public class JournalqProtocol extends Service implements ProtocolService, BrokerContextAware, ExceptionHandlerProvider {

    private JournalqConfig config;
    private JMQCoordinator coordinator;
    private JMQCoordinatorGroupManager coordinatorGroupManager;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private LongPollingManager longPollingManager;
    private JournalqContext journalqContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.config = new JournalqConfig(brokerContext.getPropertySupplier());

        this.coordinator = new JMQCoordinator(brokerContext.getCoordinatorService().getCoordinator());
        this.coordinatorGroupManager = new JMQCoordinatorGroupManager(config, brokerContext.getCoordinatorService().getOrCreateCoordinatorGroupManager(JournalqConsts.COORDINATOR_NAMESPACE));
        this.partitionAssignmentHandler = new PartitionAssignmentHandler(config, coordinatorGroupManager);
        this.longPollingManager = new LongPollingManager(brokerContext.getSessionManager(), brokerContext.getClusterManager(), brokerContext.getConsume(),brokerContext.getPropertySupplier());

        this.journalqContext = new JournalqContext(config, coordinator, coordinatorGroupManager, partitionAssignmentHandler, longPollingManager, brokerContext);
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
        return JournalqProtocolHelper.isSupport(buffer);
    }

    @Override
    public CodecFactory createCodecFactory() {
        return new JournalqCodecFactory();
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        return new JournalqCommandHandlerFactory(journalqContext);
    }

    @Override
    public String type() {
        return JournalqConsts.PROTOCOL_TYPE;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new JournalqExceptionHandler();
    }
}