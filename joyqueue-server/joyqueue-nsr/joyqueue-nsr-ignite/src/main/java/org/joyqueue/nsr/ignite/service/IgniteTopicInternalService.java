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
package org.joyqueue.nsr.ignite.service;


import com.google.inject.Inject;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.event.PartitionGroupEvent;
import org.joyqueue.event.TopicEvent;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.network.codec.NullPayloadCodec;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.codec.support.JoyQueueCodec;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.network.transport.support.DefaultTransportClientFactory;
import org.joyqueue.nsr.ignite.dao.TopicDao;
import org.joyqueue.nsr.ignite.message.IgniteMessenger;
import org.joyqueue.nsr.ignite.model.IgniteBaseModel;
import org.joyqueue.nsr.ignite.model.IgnitePartitionGroup;
import org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica;
import org.joyqueue.nsr.ignite.model.IgniteTopic;
import org.joyqueue.nsr.model.ConsumerQuery;
import org.joyqueue.nsr.model.ProducerQuery;
import org.joyqueue.nsr.model.TopicQuery;
import org.joyqueue.nsr.network.codec.OperatePartitionGroupCodec;
import org.joyqueue.nsr.network.command.CreatePartitionGroup;
import org.joyqueue.nsr.network.command.OperatePartitionGroup;
import org.joyqueue.nsr.network.command.RemovePartitionGroup;
import org.joyqueue.nsr.network.command.UpdatePartitionGroup;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.toolkit.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author lixiaobin6
 * 下午3:09 2018/8/13
 */
public class IgniteTopicInternalService implements TopicInternalService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private IgnitePartitionGroupInternalService partitionGroupService;
    @Inject
    private IgniteBrokerInternalService brokerService;
    @Inject
    private IgnitePartitionGroupReplicaInternalService partitionGroupReplicaService;
    @Inject
    private IgniteProducerInternalService producerService;
    @Inject
    private IgniteConsumerInternalService consumerService;
    @Inject
    protected IgniteMessenger messenger;

    protected TopicDao topicDao;

    private TransportClient transportClient;

    @Inject
    public IgniteTopicInternalService(TopicDao igniteDao) throws Exception {
        this.topicDao = igniteDao;
        if (transportClient == null) {
            PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
            payloadCodecFactory.register(new OperatePartitionGroupCodec());
            payloadCodecFactory.register(new NullPayloadCodec());
            JoyQueueCodec codec = new JoyQueueCodec(payloadCodecFactory);
            this.transportClient = new DefaultTransportClientFactory(codec).create(new ClientConfig());
            transportClient.start();
        }
    }
    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        return getById(IgniteTopic.getId(namespace, topic));
    }


    /**
     * 查询为订阅topic
     * @param pageQuery
     * @return
     */
    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery){
        PageResult<Topic> pageResult = new PageResult<>();
        TopicQuery topicQuery = pageQuery.getQuery();
        Pagination pagination = pageQuery.getPagination();
        pageResult.setPagination(pagination);
        List<Topic> topicList = convert(topicDao.list(pageQuery.getQuery()));
        if(topicList != null && topicList.size() >0) {
            if (topicQuery.getSubscribeType() != null) {
                if (topicQuery.getSubscribeType() == 1) {
                    ProducerQuery producerQuery = new ProducerQuery();
                    producerQuery.setTopic(topicQuery.getCode());
                    producerQuery.setApp(topicQuery.getApp());
                    List<Producer> producers = producerService.list(producerQuery);
                    if (producers != null && producers.size() > 0) {
                        List<String> producerCodeList = producers.stream().map(producer -> producer.getTopic().getCode()).collect(Collectors.toList());
                        topicList = topicList.stream().filter(topic -> !producerCodeList.contains(topic.getName().getCode())).collect(Collectors.toList());
                    }
                } else {
                    ConsumerQuery consumerQuery = new ConsumerQuery();
                    consumerQuery.setTopic(topicQuery.getCode());
                    consumerQuery.setApp(topicQuery.getApp());
                    List<Consumer> consumers = consumerService.list(consumerQuery);
                    if (consumers != null && consumers.size() > 0) {
                        List<String> consumerCodeList = consumers.stream().map(producer -> producer.getTopic().getCode()).collect(Collectors.toList());
                        topicList = topicList.stream().filter(topic -> !consumerCodeList.contains(topic.getName().getCode())).collect(Collectors.toList());
                    }
                }
            }
        }


        int start = pagination.getStart();
        int pageSize = pagination.getSize();
        int totalRecord = topicList.size();
        if (totalRecord < start) {
            return pageResult;
        }

        int end = start + pageSize;
        topicList = topicList.subList(start, end < totalRecord ? end : totalRecord);

        pageResult.setResult(topicList);
        pagination.setTotalRecord(totalRecord);
        pageResult.setPagination(pagination);

        return pageResult;
    }

    /**
     * 新增topic
     * 1.发送给所有相关broker，新添PartitionGroup命令
     * 2.碰到失败,回滚,发送给所有相关broker,删除partitionGroup命令(失败则失败，不做任何处理)
     * 数据库最终结果(成功，则topic创建，失败，则topic创建失败(如果回滚失败，则数据库不存在topic,但是存储和选举处都会有相关信息，用户生产消费不受影响，因为获取不到该topic相关元数据信息))
     *
     * @param topic topic
     * @return
     */
    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        try {
            Topic oldTopic = getTopicByCode(topic.getName().getNamespace(), topic.getName().getCode());
            if (oldTopic != null) {
                throw new Exception(String.format("topic:%s is aleady exsit",topic.getName()));
            }
            topicDao.addOrUpdate(new IgniteTopic(topic));
            //TODO 删除可能比较危险
            TopicName topicName = topic.getName();
            partitionGroupReplicaService.deleteByTopic(topicName);
            for (PartitionGroup group : partitionGroups) {
                partitionGroupService.addOrUpdate(new IgnitePartitionGroup(group));
                for (Integer brokerId : group.getReplicas()) {
                    Broker broker = (Broker) brokerService.getById(brokerId);
                    partitionGroupReplicaService.addOrUpdate(new IgnitePartitionGroupReplica(topicName, brokerId, group.getGroup()));
                    Transport transport = null;
                    Command command = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_CREATE_PARTITIONGROUP), new CreatePartitionGroup(group));
                    Command response = null;
                    try {
                        logger.info("begin createPartitionGroup topic[{}] partitionGroup[{}] [{}:{}] request[{}]",
                                topicName.getFullName(), group.getGroup(), broker.getIp(), broker.getBackEndPort(), command.getPayload());
                        transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                                broker.getBackEndPort()));
                        response = transport.sync(command);
                        logger.info("createPartitionGroup topic[{}] partitionGroup[{}] [{}:{}] request[{}] response [{}]",
                                topicName.getFullName(), group.getGroup(), broker.getIp(), broker.getBackEndPort(), command.getPayload(), response.getPayload());
                        if (JoyQueueCode.SUCCESS.getCode() != response.getHeader().getStatus()) {
                            throw new Exception(String.format("add topic [%s] error[%s]", topicName.getFullName(), response));
                        }
                    } catch (Exception e) {
                        logger.error("createPartitionGroup error request[{}] response [{}],rollback", command.getPayload(), response, e);
                        throw new Exception(String.format("add topic [%s] error ", topicName.getFullName()), e);
                    } finally {
                        if (null != transport) {
                            try {
                                transport.stop();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
            this.publishEvent(TopicEvent.add(topicName));

        } catch (Exception e) {
            logger.error("add topic error",e);
            for (PartitionGroup group : partitionGroups) {
                for (Integer brokerId : group.getReplicas()) {
                    Broker broker = (Broker) brokerService.getById(brokerId);
                    Command command = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_CREATE_PARTITIONGROUP), new CreatePartitionGroup(group, true));
                    Transport transport = null;
                    Command response = null;
                    try {
                        transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                                broker.getBackEndPort()));
                        response = transport.sync(command);
                        logger.info("remove partitionGroup request[{}] response [{}]", command.getPayload(), response.getPayload());
                    } catch (TransportException ignore) {
                        logger.error("remove partitionGroup error request[{}] response [{}]", command.getPayload(), response, ignore);
                    } finally {
                        if (null != transport){
                            transport.stop();
                        }
                    }
                }
            }
            throw new RuntimeException(String.format("add topic error"), e);
        }
    }

    /**
     * 删除topic
     * 1.发送该给所有相关的broker,删除partitionGroup命令
     * 2.碰到失败，无需回滚操作
     * 数据库最终结果(成功，存储和选举可能会有异常不同步数据，用户使用不受影响，因为已经获取不到topic元数据信息)
     *
     * @param topic topic
     * @return
     */
    @Override
    public void removeTopic(Topic topic) {
        TopicName topicName = topic.getName();
        try {
            List<PartitionGroup> partitionGroups = partitionGroupService.getByTopic(topicName);
            topicDao.deleteById(topicName.getFullName());
            partitionGroupReplicaService.deleteByTopic(topicName);
            if (null != partitionGroups){
                for (PartitionGroup group : partitionGroups) {
                    partitionGroupService.delete(((IgnitePartitionGroup) group).getId());
                    for (Integer brokerId : group.getReplicas()) {
                        Broker broker = (Broker) brokerService.getById(brokerId);
                        Transport transport = null;
                        Command command = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_REMOVE_PARTITIONGROUP), new RemovePartitionGroup(group));
                        Command response = null;
                        try {
                            transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                                    broker.getBackEndPort()));
                            response = transport.sync(command);
                            logger.info("remove partitionGroup request[{}] response [{}]", command, response);
                            if (JoyQueueCode.SUCCESS.getCode() != ((JoyQueueHeader) response.getHeader()).getStatus()) {
                                throw new Exception(String.format("remove topic [%s] error ", topicName.getFullName(), response.getPayload()));
                            }
                        } catch (Exception ignore) {
                            logger.error("remove partitionGroup error request[{}] response [{}]", command, response, ignore);
                        } finally {
                            if (null != transport){
                                transport.stop();
                            }
                        }
                    }
                }
            }
            this.publishEvent(TopicEvent.remove(topicName));
        } catch (Exception e) {
            String msg = String.format("remove topic error[%s]", topicName.getFullName());
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * 新增partitionGroup
     * 1.发送给所有相关broker，新添PartitionGroup命令
     * 2.碰到失败,回滚,发送给所有相关broker,删除partitionGroup命令(失败则失败，不做任何处理)
     * 数据库最终结果(成功，则partitionGroup创建，失败，则topic创建失败(如果回滚失败，则数据库不存在topic,但是存储和选举处都会有相关信息，选举通知nsr选举结果部分需要注意,用户生产消费不受影响，因为获取不到该topic下面的partitionGroup相关元数据信息))
     *
     * @param group
     * @return
     */
    @Override
    public void addPartitionGroup(PartitionGroup group) {
        Command command = null;
        Transport transport = null;
        Command response = null;
        try {

            if (group.getElectType().type() == PartitionGroup.ElectType.fix.type()) {
                group.setLeader(group.getReplicas().iterator().next());
            }
            Topic topic = getById(group.getTopic().getFullName());
            //topic.get
            topic.setPartitions((short) (topic.getPartitions()+group.getPartitions().size()));
            topicDao.addOrUpdate(new IgniteTopic(topic));
            partitionGroupService.addOrUpdate(new IgnitePartitionGroup(group));
            for (Integer brokerId : group.getReplicas()) {
                Broker broker = (Broker) brokerService.getById(brokerId);
                partitionGroupReplicaService.addOrUpdate(new IgnitePartitionGroupReplica(group.getTopic(), brokerId, group.getGroup()));
                try {
                    transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                            broker.getBackEndPort()));
                    command = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_CREATE_PARTITIONGROUP), new CreatePartitionGroup(group));
                    response = transport.sync(command);
                    logger.info("create partitionGroup request[{}] response [{}]", command, response);
                    if (JoyQueueCode.SUCCESS.getCode() != ((JoyQueueHeader) response.getHeader()).getStatus()) {
                        throw new Exception(String.format("add topic [{}] error ", group.getTopic(), response));
                    }
                } catch (Exception e) {
                    logger.error("create partitionGroup error request[{}] response [{}]", command, response, e);
                    throw new Exception(String.format("add topic [{}] error ", group.getTopic(), e));
                } finally {
                    if (null != transport){
                        transport.stop();
                    }
                }
            }
            this.publishEvent(PartitionGroupEvent.add(group.getTopic(), group.getGroup()));
        } catch (Exception e) {
            logger.error("add topic partition group ",e);
            for (Integer brokerId : group.getReplicas()) {
                Broker broker = (Broker) brokerService.getById(brokerId);
                try {
                    transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                            broker.getBackEndPort()));
                    command = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_CREATE_PARTITIONGROUP), new CreatePartitionGroup(group, true));
                    response = transport.sync(command);
                    logger.info("remove partitionGroup request[{}] response [{}]", command, response);
                } catch (TransportException ignore) {
                    logger.error("remove partitionGroup error request[{}] response [{}]", command, response, ignore);
                } finally {
                    if (null != transport){
                        transport.stop();
                    }
                }
            }
            throw new RuntimeException("add topic error", e);
        }
    }

    /**
     * 删除partitionGroup
     * 1.发送该给所有相关的broker,删除partitionGroup命令
     * 2.碰到失败，无需回滚
     * 数据库最终结果(成功，存储和选举可能会有异常不同步数据，用户使用不受影响，因为已经获取不到topic该partitionGroup元数据信息)
     *
     * @param group
     * @return
     */
    @Override
    public void removePartitionGroup(PartitionGroup group) {
        Command command = null;
        Transport transport = null;
        TopicName topicName = group.getTopic();
        int groupNo = group.getGroup();
        try {

            Topic topic = getById(topicName.getFullName());
            topic.setPartitions((short) (topic.getPartitions()-group.getPartitions().size()));
            topicDao.addOrUpdate(new IgniteTopic(topic));

            partitionGroupReplicaService.deleteByTopicAndPartitionGroup(topicName, groupNo);
            group = partitionGroupService.getById(topicName.getFullName() + IgniteBaseModel.SPLICE + group.getGroup());
            if(null==group){
                logger.error("topic {} group {} not exist",topicName.getFullName(),groupNo);
                return;
            }
            partitionGroupService.delete(topicName.getFullName() + IgniteBaseModel.SPLICE + group.getGroup());
            for (Integer brokerId : group.getReplicas()) {
                Broker broker = brokerService.getById(brokerId);
                command = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_REMOVE_PARTITIONGROUP), new RemovePartitionGroup(group));
                Command response = null;
                try {
                    transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                            broker.getBackEndPort()));
                    response = transport.sync(command);
                        logger.info("remove partitionGroup request[{}] response [{}]", command.getPayload(), response.getPayload());
                    if (JoyQueueCode.SUCCESS.getCode() != ((JoyQueueHeader) response.getHeader()).getStatus()) {
                        throw new Exception(String.format("remove topic [{}] error ", group.getTopic(), response.getPayload()));
                    }
                } catch (Exception e) {
                    logger.error("remove partitionGroup error request[{}] response [{}]", command, response, e);
                } finally {
                    if (null != transport){
                        transport.stop();
                    }
                }
            }
            this.publishEvent(PartitionGroupEvent.remove(group.getTopic(), group.getGroup()));
        } catch (Exception e) {
            throw new RuntimeException("add topic error", e);
        }
    }
    @Override
    public void leaderChange(PartitionGroup group) {
        Command command = null;
        Transport transport = null;
        TopicName topicName = group.getTopic();
        Integer leader = group.getLeader();
        int groupNo = group.getGroup();
        try {
            group = partitionGroupService.getById(topicName.getFullName() + IgniteBaseModel.SPLICE + group.getGroup());
            if(null==group){
                logger.error("topic {} group {} not exist",topicName.getFullName(),groupNo);
                return;
            }
            if(-1==group.getLeader()){
                logger.error("topic {} group {} leader is -1",topicName.getFullName(),groupNo);
                return;
            }
            Broker broker = brokerService.getById(group.getLeader());
            group.setLeader(leader);
            command = new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_LEADERCHANAGE_PARTITIONGROUP), new UpdatePartitionGroup(group));
            Command response = null;
            try {
                transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                        broker.getBackEndPort()));
                response = transport.sync(command);
                logger.info("leaderChange partitionGroup request[{}] response [{}]", command.getPayload(), response.getPayload());
                if (JoyQueueCode.SUCCESS.getCode() != ((JoyQueueHeader) response.getHeader()).getStatus()) {
                    throw new Exception(String.format("leaderChange  [{}] error [{}]", group));
                }
            }  finally {
                if (null != transport){
                    transport.stop();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("leaderChange partitionGroup error", e);
        }
    }
    /**
     * 更新partitionGroup(保证执行成功或者失败后客户端根据获取到的元数据是可以正常消费和生产的)
     * 一.更新partitions
     * 1.通知所有的broker节点，调用存储的rePartition接口(有一个失败则认为失败)
     * 2.，失败，则回滚,调用存储 rePartition接口，失败则不处理
     * 3.数据库最终结果(成功，则成功，失败则失败，没通知成功的节点数据会和数据库不一致，用户生产和消费可能会出问题)
     * 二.添加broker节点(节点数量必须是偶数)
     * 1.第一轮,第一个节点添加 通知所有的broker节点，如果是老的节点,调用选举接口和存储接口告知该partitionGroup新增加了一个节点，如果是新增的那个节点，则调用选举接口和存储接口告知该broker上新添加了一个partitionGroup(半数通知成功，则认为成功,否则失败)
     * 2.第二轮,第二个节点添加 通知所有的broker节点，如果是老的节点,调用选举接口和存储接口告知该partitionGroup新增加了一个节点，如果是新增的那个节点，则调用选举接口和存储接口告知该broker上新添加了一个partitionGroup(半数通知成功，则认为成功，否则失败)
     * 3.第一步失败，无需回滚，直接失败,如果第二步失败,则需要回滚第一步，第一步回滚时候，如果是老的节点，调用选举接口和存储接口告知该partitionGroup删除了一个节点，如果是新增加的那个节点，则调用选举接口和存储接口告知该broker上面删除一个partitionGroup(失败则失败)
     * 4.数据库最终结果(成功，则成功，失败则失败，没通知成功的节点数据会和数据库不一致,如果该过程总触发选举，则用户生产和消费会有影响)
     * )
     * 三:删除broker节点(节点数必须是偶数)
     * 1.第一轮,第一个节点删除 通知所有的broker节点 ，如果是老的节点，调用选举接口和存储接口告知该partitionGroup删除了一个节点，如果是被删除的那个节点，则调用选举接口和存储接口告知该broker上面删除一个partitionGroup(半数通知成功，则认为成功)
     * 2.第二轮,第二个节点删除 通知所有的broker节点 ，如果是老的节点，调用选举接口和存储接口告知该partitionGroup删除了一个节点，如果是被删除的那个节点，则调用选举接口和存储接口告知该broker上面删除一个partitionGroup(半数通知成功，则认为成功)
     * 3.第一步失败，无需回滚,直接失败,如果第二步失败，则需要回滚第一步,第一步回滚时候，如果是老的节点，调用选举接口和存储接口告知该partitionGroup新增加了一个节点，如果是被删除的那个节点，则调用选举接口和存储接口告知该broker上面新增加一个partitionGroup(失败则失败)
     * 4.数据库最终结果(成功，则成功，失败则失败，没通知成功的节点数据会和数据库不一致,如果该过程总触发选举，则用户生产和消费会有影响)
     *
     * @return
     */
    public Collection<Integer> updatePartitionGroup(PartitionGroup group) {
        List<Pair<Set<Integer>, Command>> commands = new ArrayList<>();
        List<Pair<Set<Integer>, Command>> rollbackCommands = new ArrayList<>();
        try {
            final PartitionGroup groupOld = partitionGroupService.getById(group.getTopic().getFullName() + IgniteBaseModel.SPLICE + group.getGroup());
            final PartitionGroup groupNew = group;
            groupNew.setLeader(groupOld.getLeader());
            groupNew.setIsrs(groupOld.getIsrs());
            groupNew.setTerm(groupOld.getTerm());
            PartitionGroup groupToUpdae = groupOld;
            if (groupNew.getPartitions().size() != groupOld.getPartitions().size()) {
                //更新topic  partitions
                Topic topic = getById(group.getTopic().getFullName());
                topic.setPartitions((short) (topic.getPartitions()+(groupNew.getPartitions().size()- groupOld.getPartitions().size())));
                topicDao.addOrUpdate(new IgniteTopic(topic));

                groupToUpdae.setPartitions(groupNew.getPartitions());
                commands.add(new Pair(groupOld.getReplicas(), new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_UPDATE_PARTITIONGROUP), new UpdatePartitionGroup(groupToUpdae))));
            } else {
                commands.add(null);
            }
            Set<Integer> replicasAdd = new TreeSet<>(groupNew.getReplicas());
            Set<Integer> replicasRemove = new TreeSet<>(groupOld.getReplicas());
            replicasAdd.removeAll(replicasRemove);
            replicasRemove.removeAll(new TreeSet<>(groupNew.getReplicas()));
            Set<Integer> brokerAddToNotice = new TreeSet<>(groupOld.getReplicas());
            brokerAddToNotice.addAll(replicasAdd);
            for (Integer brokerId : replicasAdd) {
                groupToUpdae = groupToUpdae.clone();
                groupToUpdae.getReplicas().add(brokerId);
                commands.add(new Pair<>(brokerAddToNotice, new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_UPDATE_PARTITIONGROUP), new UpdatePartitionGroup(groupToUpdae))));
                partitionGroupReplicaService.addOrUpdate(new IgnitePartitionGroupReplica(group.getTopic(), brokerId, group.getGroup()));
            }
            Set<Integer> brokerAddToRemove = new TreeSet<>(brokerAddToNotice);
            for (Integer brokerId : replicasRemove) {
                groupToUpdae = groupToUpdae.clone();
                groupToUpdae.getReplicas().remove(brokerId);
                brokerAddToRemove = new TreeSet<>(brokerAddToRemove);
                commands.add(new Pair<>(new TreeSet<>(brokerAddToRemove),
                        new Command(new JoyQueueHeader(Direction.REQUEST, CommandType.NSR_UPDATE_PARTITIONGROUP), new UpdatePartitionGroup(groupToUpdae))));
                brokerAddToRemove.remove(brokerId);
                partitionGroupReplicaService.delete(new IgnitePartitionGroupReplica(group.getTopic(), brokerId, group.getGroup()).getId());
            }
            if(null==groupToUpdae.getReplicas()||groupToUpdae.getReplicas().size()<1){
                group.setLeader(-1);
                group.setTerm(0);
            }
            for (int i = 0; i < commands.size(); i++) {
                Pair<Set<Integer>, Command> command = commands.get(i);
                int updateCount = 0;
                Transport transport = null;
                Command response = null;
                if (null != command) {
                    for (Integer brokerId : command.getKey()) {
                        if(group.getOutSyncReplicas().contains(brokerId)){
                            continue;
                        }
                        Broker broker = brokerService.getById(brokerId);
                        try {
                            transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                                    broker.getBackEndPort()));
                            response = transport.sync(command.getValue());
                            logger.info("update partitionGroup broker[{}] request[{}] response [{}]",
                                    broker.getIp() + ":" + broker.getPort(), command.getValue().getPayload(), response.getHeader().getStatus());
                            if (JoyQueueCode.SUCCESS.getCode() != response.getHeader().getStatus()) {
                                throw new Exception(String.format("update partitionGroup broker[%s] request[%s] response [%s]r ",
                                        broker.getIp() + ":" + broker.getBackEndPort(), group.getTopic(), response.getPayload()));
                            }
                            updateCount++;
                            if (groupNew.getReplicas().contains(brokerId)) {
                                partitionGroupReplicaService.addOrUpdate(new IgnitePartitionGroupReplica(group.getTopic(), brokerId, group.getGroup()));
                            }
                        } catch (Exception ignore) {
                            logger.error(String.format("update partitionGroup error broker[%s] request[%s] response [%s]",
                                    broker.getIp() + ":" + broker.getBackEndPort(), command.getValue().getPayload(), response),
                                    ignore);
                        } finally {
                            if (null != transport) {
                                transport.stop();
                            }
                        }
                    }
                    rollbackCommands.add(command);
                    if (updateCount != command.getKey().size()) {
                        //第0个命令是repartitions命令,必须是全部成功
                        if (i == 0) {
                            throw new Exception(String.format("rePartitionGroup error topic[%s], rollback", group.getTopic()));
                        }else {
                            throw new Exception(String.format("upPartitionGroup error topic[%s], rollback", group.getTopic()));
                        }
                    }
                }
            }
            partitionGroupService.addOrUpdate(new IgnitePartitionGroup(group));
            this.publishEvent(PartitionGroupEvent.update(group.getTopic(), group.getGroup()));
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("update partitiongroup error", e);
            for (int i = 0; i < rollbackCommands.size(); i++) {
                Pair<Set<Integer>, Command> command = rollbackCommands.get(i);
                Transport transport = null;
                Command response = null;
                if (null != command) {
                    ((OperatePartitionGroup) command.getValue().getPayload()).rollback(true);
                    try {
                        for (Integer brokerId : command.getKey()) {
                            Broker broker = (Broker) brokerService.getById(brokerId);
                            transport = transportClient.createTransport(new InetSocketAddress(broker.getIp(),
                                    broker.getBackEndPort()));
                            response = transport.sync(command.getValue());
                            logger.info("remove partitionGroup request[{}] response [{}]", command, response);
                        }
                    } catch (Exception ignore) {
                        logger.error("update partitionGroup error request[{}] response [{}]", command, response, ignore);
                    } finally {
                        if (null != transport){
                            transport.stop();
                        }
                    }
                }
            }
            throw new RuntimeException("update topic PartitionGroup error", e);
        }
    }

    @Override
    public void leaderReport(PartitionGroup group) {
        partitionGroupService.update(group);
        this.publishEvent(PartitionGroupEvent.update(group.getTopic(), group.getGroup()));
    }

    public void publishEvent(MetaEvent event) {
        logger.info("publishEvent {}",event);
        messenger.publish(event);
    }

    /**
     * 删除partitionGroup
     * 1.发送该给所有相关的broker,删除partitionGroup命令
     * 2.碰到失败，无需回滚
     * 数据库最终结果(成功，存储和选举可能会有异常不同步数据，用户使用不受影响，因为已经获取不到topic该partitionGroup元数据信息)
     *
     * @return
     */
    @Override
    public List<PartitionGroup> getPartitionGroup(String namespace, String topic, Object[] groups) {
        List<PartitionGroup> list = new ArrayList<>();
        for (Object group : groups) {
            PartitionGroup partitionGroup = partitionGroupService.getByTopicAndGroup(new TopicName(topic, namespace), Integer.parseInt(group.toString()));
            if (null != partitionGroup){
                list.add(partitionGroup);
            }
        }
        return list;
    }

    @Override
    public Topic add(Topic topic) {
        return null;
    }

    @Override
    public Topic update(Topic topic) {
        return null;
    }

    //@Override
    public IgniteTopic toIgniteModel(Topic model) {
        return new IgniteTopic(model);
    }

    @Override
    public Topic getById(String id) {
        return topicDao.findById(id);
    }

    @Override
    public PageResult<Topic> search(QPageQuery<TopicQuery> pageQuery) {
        PageResult<IgniteTopic> topics = topicDao.pageQuery(pageQuery);
        PageResult<Topic> result = new PageResult<>();
        result.setResult(convert(topics.getResult()));
        result.setPagination(topics.getPagination());
        return result;
    }

    @Override
    public List<Topic> getAll() {
        return convert(topicDao.list(null));
    }

    public static final List<Topic> convert(List<IgniteTopic> igniteTopics) {
        if (igniteTopics != null && !igniteTopics.isEmpty()) {
            List<Topic> resultData = new ArrayList<>();
            igniteTopics.forEach(e -> resultData.add(e));
            return resultData;
        }

        return Collections.emptyList();
    }
}
