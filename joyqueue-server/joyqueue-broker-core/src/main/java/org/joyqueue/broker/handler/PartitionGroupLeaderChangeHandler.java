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
package org.joyqueue.broker.handler;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.nsr.network.command.UpdatePartitionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2019/3/7
 */
@Deprecated
public class PartitionGroupLeaderChangeHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(PartitionGroupLeaderChangeHandler.class);
    private ElectionService electionService;
    private NameServiceConfig config;

    public PartitionGroupLeaderChangeHandler(BrokerContext brokerContext){
        this.electionService = brokerContext.getElectionService();
        this.config = new NameServiceConfig(brokerContext.getPropertySupplier());
    }
    @Override
    public int type() {
        return NsrCommandType.NSR_LEADERCHANAGE_PARTITIONGROUP;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        if (!config.getMessengerIgniteEnable()) {
            return BooleanAck.build();
        }
        if (command == null) {
            logger.error("PartitionGroupLeaderChangeHandler request command is null");
            return null;
        }
        UpdatePartitionGroup request = (UpdatePartitionGroup) command.getPayload();
        PartitionGroup group = request.getPartitionGroup();
        try {
            electionService.onLeaderChange(group.getTopic(),group.getGroup(),group.getLeader());
        } catch (Exception e) {
            logger.error(String.format("PartitionGroupLeaderChangeHandler request command[%s] error", command.getPayload()), e);
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR, e.getMessage());
        }
        return BooleanAck.build();
    }
}
