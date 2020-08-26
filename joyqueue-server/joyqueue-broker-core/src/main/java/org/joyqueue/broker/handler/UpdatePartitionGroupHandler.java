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

import com.alibaba.fastjson.JSON;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.nsr.network.command.UpdatePartitionGroup;
import org.joyqueue.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2018/10/8
 */
@Deprecated
public class UpdatePartitionGroupHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(CreatePartitionGroupHandler.class);
    private ClusterManager clusterManager;
    private StoreService storeService;
    private NameServiceConfig config;

    public UpdatePartitionGroupHandler(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.storeService = brokerContext.getStoreService();
        this.config = new NameServiceConfig(brokerContext.getPropertySupplier());
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_UPDATE_PARTITIONGROUP;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        if (!config.getMessengerIgniteEnable()) {
            return BooleanAck.build();
        }
        if (command == null) {
            logger.error("UpdatePartitionGroupHandler request command is null");
            return null;
        }
        UpdatePartitionGroup request = (UpdatePartitionGroup) command.getPayload();
        PartitionGroup groupNew = request.getPartitionGroup();
        PartitionGroup groupOld = clusterManager.getNameService().getTopicConfig(groupNew.getTopic()).fetchPartitionGroupByGroup(groupNew.getGroup());
        try {
            Integer localBrokerId = clusterManager.getBrokerId();
            logger.info("begin updatePartitionGroup topic[{}] from [{}] to [{}]",
                    groupNew.getTopic(), JSON.toJSONString(groupOld), JSON.toJSONString(groupNew));
            if (!request.isRollback()) {
                commit(groupNew, groupOld, localBrokerId);
            } else {
                rollback(groupNew, groupOld, localBrokerId);
            }
            return BooleanAck.build();
        } catch (Exception e) {
            logger.error(String.format("UpdatePartitionGroupHandler request command[%s] error", command.getPayload()), e);
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR, e.getMessage());
        }
    }


    private void commit(PartitionGroup groupNew, PartitionGroup groupOld, Integer localBrokerId) throws Exception {
        logger.info("OnUpdatePartitionGroup, from: [{}] to [{}].", groupOld, groupNew);
        if(groupOld.getReplicas().contains(localBrokerId) || groupNew.getReplicas().contains(localBrokerId)) {
            // 先处理副本变更
            if(!groupOld.getReplicas().equals(groupNew.getReplicas())) {
                storeService.maybeUpdateReplicas(groupNew.getTopic().getFullName(), groupNew.getGroup(), groupNew.getReplicas());
            }
            // 再处理分区变更
            if(!groupOld.getPartitions().equals(groupNew.getPartitions())) {
                storeService.maybeRePartition(groupNew.getTopic().getFullName(), groupNew.getGroup(), groupNew.getPartitions());
            }
        }
    }

    private void rollback(PartitionGroup groupNew, PartitionGroup groupOld,Integer localBrokerId) throws Exception {
        logger.info("On rollback PartitionGroup, from: [{}] to [{}].", groupOld, groupNew);
        if(groupOld.getReplicas().contains(localBrokerId) || groupNew.getReplicas().contains(localBrokerId)) {
            // 先处理副本变更
            if(!groupOld.getReplicas().equals(groupNew.getReplicas())) {
                storeService.maybeUpdateReplicas(groupOld.getTopic().getFullName(), groupOld.getGroup(), groupOld.getReplicas());
            }
            // 再处理分区变更
            if(!groupOld.getPartitions().equals(groupNew.getPartitions())) {
                storeService.maybeRePartition(groupOld.getTopic().getFullName(), groupOld.getGroup(), groupOld.getPartitions());
            }
        }
    }
}
