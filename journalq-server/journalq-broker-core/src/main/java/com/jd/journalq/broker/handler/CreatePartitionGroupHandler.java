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
package com.jd.journalq.broker.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.nsr.network.command.CreatePartitionGroup;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/8
 */
public class CreatePartitionGroupHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(CreatePartitionGroupHandler.class);
    private ClusterManager clusterManager;
    private ElectionService electionService;
    private StoreService storeService;
    public CreatePartitionGroupHandler(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.electionService = brokerContext.getElectionService();
        this.storeService = brokerContext.getStoreService();
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_CREATE_PARTITIONGROUP;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        if (command == null) {
            logger.error("CreatePartitionGroupHandler request command is null");
            return null;
        }
        CreatePartitionGroup request = ((CreatePartitionGroup)command.getPayload());
        try{
            PartitionGroup group = request.getPartitionGroup();
            if(logger.isDebugEnabled())logger.debug("begin createPartitionGroup topic[{}] partitionGroupRequest [{}] ",group.getTopic(),JSON.toJSONString(request));
            if (!request.isRollback()) {
                commit(group);
            } else {
                rollback(group);
            }
            return BooleanAck.build();
        }catch (JMQException e) {
            logger.error(String.format("CreatePartitionGroupHandler request command[%s] error",request),e);
            return BooleanAck.build(e.getCode(),e.getMessage());
        } catch (Exception e) {
            logger.error(String.format("CreatePartitionGroupHandler request command[%s] error",request),e);
            return BooleanAck.build(JMQCode.CN_UNKNOWN_ERROR,e.getMessage());
        }
    }

    private void commit(PartitionGroup group) throws Exception {
        if(logger.isDebugEnabled())logger.debug("topic[{}] add partitionGroup[{}]",group.getTopic(),group.getGroup());
        //if (!storeService.partitionGroupExists(group.getTopic(),group.getGroup())) {
            storeService.createPartitionGroup(group.getTopic().getFullName(), group.getGroup(), Shorts.toArray(group.getPartitions()), Ints.toArray(group.getReplicas()));
            //}
            Set<Integer> replicas = group.getReplicas();
            List<Broker> list = new ArrayList<>(replicas.size());
            replicas.forEach(brokerId->{
                list.add(clusterManager.getBrokerById(brokerId));
            });
            electionService.onPartitionGroupCreate(group.getElectType(),group.getTopic(),group.getGroup(),list,group.getLearners(),clusterManager.getBrokerId(),group.getLeader());
    }
    private void rollback(PartitionGroup group){
        if(logger.isDebugEnabled())logger.debug("topic[{}] remove partitionGroup[{}]",group.getTopic(),group.getGroup());
            storeService.removePartitionGroup(group.getTopic().getFullName(),group.getGroup());
            electionService.onPartitionGroupRemove(group.getTopic(),group.getGroup());
    }
}
