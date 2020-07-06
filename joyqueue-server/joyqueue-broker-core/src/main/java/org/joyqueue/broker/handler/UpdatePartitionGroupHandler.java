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
import com.google.common.primitives.Shorts;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author wylixiaobin
 * Date: 2018/10/8
 */
@Deprecated
public class UpdatePartitionGroupHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(CreatePartitionGroupHandler.class);
    private ClusterManager clusterManager;
    private ElectionService electionService;
    private StoreService storeService;
    private NameServiceConfig config;

    public UpdatePartitionGroupHandler(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.electionService = brokerContext.getElectionService();
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
            Set<Integer> replicasNew = new TreeSet<>(groupNew.getReplicas());
            Set<Integer> replicasOld = new TreeSet<>(groupOld.getReplicas());
            replicasNew.removeAll(groupOld.getReplicas());
            replicasOld.removeAll(groupNew.getReplicas());
            Integer localBrokerId = clusterManager.getBrokerId();
            logger.info("begin updatePartitionGroup topic[{}] from [{}] to [{}] addNode[{}] removeNode[{}] localNode[{}]",
                    groupNew.getTopic(), JSON.toJSONString(groupOld), JSON.toJSONString(groupNew), Arrays.toString(replicasNew.toArray()), Arrays.toString(replicasOld.toArray()));
            if (!request.isRollback()) {
                commit(groupNew, groupOld, replicasNew, replicasOld, localBrokerId);
            } else {
                rollback(groupNew, groupOld, replicasNew, replicasOld, localBrokerId);
            }
            return BooleanAck.build();
        } catch (Exception e) {
            logger.error(String.format("UpdatePartitionGroupHandler request command[%s] error", command.getPayload()), e);
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR, e.getMessage());
        }
    }


    private void commit(PartitionGroup groupNew, PartitionGroup groupOld, Set<Integer> nodeAdd, Set<Integer> nodeRemove, Integer localBrokerId) throws Exception {
        List<Broker> brokers = new ArrayList<>(groupNew.getReplicas().size());
        groupNew.getReplicas().forEach(replica -> {
            brokers.add(clusterManager.getBrokerById(replica));
        });
        TopicName topicName = groupNew.getTopic();
        String topic = topicName.getFullName();
        for (Integer brokerId : nodeAdd) {
            if (localBrokerId.equals(brokerId)) {
                logger.info("topic[{}] add partitionGroup[{}]", topic, groupNew.getGroup());
                storeService.createPartitionGroup(topic, groupNew.getGroup(), Shorts.toArray(groupNew.getPartitions()));
                electionService.onPartitionGroupCreate(groupNew.getElectType(), topicName, groupNew.getGroup(), brokers, groupNew.getLearners(), clusterManager.getBrokerId(), groupNew.getLeader());
            } else {
                logger.info("topic[{}] update partitionGroup[{}] add node[{}] ", topic, groupNew.getGroup(), brokerId);
                electionService.onNodeAdd(topicName, groupNew.getGroup(), groupNew.getElectType(),
                        brokers, groupNew.getLearners(), clusterManager.getBrokerById(brokerId),
                        localBrokerId, groupNew.getLeader());
                storeService.rePartition(topic, groupNew.getGroup(), groupNew.getPartitions().toArray(new Short[groupNew.getPartitions().size()]));
            }
        }
        if(groupOld.getPartitions().size()!=groupNew.getPartitions().size()){
            storeService.rePartition(topic, groupNew.getGroup(), groupNew.getPartitions().toArray(new Short[groupNew.getPartitions().size()]));
        }
        for (Integer brokerId : nodeRemove) {
            if (localBrokerId.equals(brokerId)) {
                logger.info("topic[{}] remove partitionGroup[{}]", topic, groupNew.getGroup());
                storeService.removePartitionGroup(topic, groupNew.getGroup());
                electionService.onPartitionGroupRemove(topicName, groupNew.getGroup());
            } else {
                logger.info("topic[{}] update partitionGroup[{}] remove node[{}] ", topic, groupNew.getGroup(), brokerId);
                electionService.onNodeRemove(topicName, groupNew.getGroup(), brokerId, localBrokerId);
                storeService.rePartition(topic, groupNew.getGroup(), groupNew.getPartitions().toArray(new Short[groupNew.getPartitions().size()]));
            }
        }
    }

    private void rollback(PartitionGroup groupNew, PartitionGroup groupOld, Set<Integer> nodeAdd, Set<Integer> nodeRemove, Integer localBrokerId) throws Exception {

        TopicName topicName = groupNew.getTopic();
        String topic = topicName.getFullName();
        for (Integer brokerId : nodeAdd) {
            if (localBrokerId.equals(brokerId)) {
                logger.info("topic[{}] remove partitionGroup[{}]", groupNew.getTerm(), groupNew.getGroup());
                storeService.removePartitionGroup(topic, groupNew.getGroup());
                electionService.onPartitionGroupRemove(topicName, groupNew.getGroup());
            } else {
                logger.info("topic[{}] update partitionGroup[{}] remove node[{}] ", groupNew.getTerm(), groupNew.getGroup(), brokerId);
                electionService.onNodeRemove(topicName, groupNew.getGroup(), brokerId, localBrokerId);
                storeService.rePartition(groupOld.getTopic().getFullName(), groupOld.getGroup(), groupOld.getPartitions().toArray(new Short[groupOld.getPartitions().size()]));
            }
        }
        List<Broker> brokers = new ArrayList<>(groupOld.getReplicas().size());
        groupNew.getReplicas().forEach(replica -> {
            brokers.add(clusterManager.getBrokerById(replica));
        });
        for (Integer brokerId : nodeRemove) {
            if (localBrokerId.equals(brokerId)) {
                logger.info("topic[{}] add partitionGroup[{}]", groupNew.getTerm(), groupNew.getGroup());
                storeService.createPartitionGroup(topic, groupNew.getGroup(), Shorts.toArray(groupNew.getPartitions()));
                electionService.onPartitionGroupCreate(groupNew.getElectType(), topicName, groupNew.getGroup(), brokers, groupNew.getLearners(), clusterManager.getBrokerId(), groupNew.getLeader());
            } else {
                logger.info("topic[{}] update partitionGroup[{}] add node[{}] ", groupNew.getTerm(), groupNew.getGroup(), brokerId);
                electionService.onNodeAdd(topicName, groupNew.getGroup(), groupNew.getElectType(),
                        brokers, groupNew.getLearners(), clusterManager.getBrokerById(brokerId),
                        localBrokerId, groupNew.getLeader());
                storeService.rePartition(groupOld.getTopic().getFullName(), groupOld.getGroup(), groupOld.getPartitions().toArray(new Short[groupOld.getPartitions().size()]));
            }
        }
        if(groupOld.getPartitions().size()!=groupNew.getPartitions().size()){
            storeService.rePartition(topic, groupOld.getGroup(), groupOld.getPartitions().toArray(new Short[groupOld.getPartitions().size()]));
        }
    }
}
