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
package org.joyqueue.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.convert.CodeConverter;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.ServiceException;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.AppUnsubscribedTopic;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.BrokerGroup;
import org.joyqueue.model.domain.Consumer;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.Namespace;
import org.joyqueue.model.domain.PartitionGroupReplica;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.model.exception.DuplicateKeyException;
import org.joyqueue.model.query.QTopic;
import org.joyqueue.nsr.ConsumerNameServerService;
import org.joyqueue.nsr.PartitionGroupServerService;
import org.joyqueue.nsr.ProducerNameServerService;
import org.joyqueue.nsr.ReplicaServerService;
import org.joyqueue.nsr.TopicNameServerService;
import org.joyqueue.service.TopicService;
import org.joyqueue.util.EnvironmentUtil;
import org.joyqueue.util.NullUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 主题服务实现
 * Created by chenyanying3 on 2018-10-18.
 */
@Service("topicService")
public class TopicServiceImpl implements TopicService {
    private final Logger logger = LoggerFactory.getLogger(TopicServiceImpl.class);

    @Autowired
    private TopicNameServerService topicNameServerService;
    @Autowired
    protected ConsumerNameServerService consumerNameServerService;
    @Autowired
    protected ProducerNameServerService producerNameServerService;
    @Autowired
    protected PartitionGroupServerService partitionGroupServerService;
    @Autowired
    protected ReplicaServerService replicaServerService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void addWithBrokerGroup(Topic topic, BrokerGroup brokerGroup, List<Broker> brokers, Identity operator) {
        Namespace namespace = topic.getNamespace();
        Topic oldTopic = findByCode(namespace == null?null:namespace.getCode(),topic.getCode());
        if (oldTopic != null) {
            throw new DuplicateKeyException("topic aleady exist");
        }

        if (EnvironmentUtil.isTest()) {
            topic.setElectType(TopicPartitionGroup.ElectType.fix.type());
            brokers = Lists.newArrayList(brokers.get(0));
        }

        List<TopicPartitionGroup> partitionGroups = addPartitionGroup(topic, brokers);
        try {
            topicNameServerService.addTopic(topic, partitionGroups);
        } catch (Exception e) {
            String errorMsg = "新建主题，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg, e);//回滚
        }
    }

    private List<TopicPartitionGroup> addPartitionGroup(Topic topic, List<Broker> brokers) {
        //partitionGrouop 默认是broker数
        //当partition小于broker数时,partitiongroup应该为partitiongroup数,保证每个partitiongroup都能有partition
        int partitionGroupNum  = brokers.size();
        if (topic.getPartitions() < brokers.size()) {
            partitionGroupNum = topic.getPartitions();
        }

        int step = topic.getPartitions()/partitionGroupNum;
        int surplus = topic.getPartitions()%partitionGroupNum;
        int index = 0;
        List partitions = new ArrayList(topic.getPartitions());
        for(int i = 0;i<topic.getPartitions();i++){
            partitions.add(i);
        }
        List<TopicPartitionGroup> partitionGroups = new ArrayList<>(partitionGroupNum);
        for(int i = 0,j=0;i<partitionGroupNum;i++,j++){
            //创建partitiongroup
            TopicPartitionGroup partitionGroup = new TopicPartitionGroup();
            partitionGroup.setNamespace(topic.getNamespace());
            partitionGroup.setTopic(topic);
            partitionGroup.setGroupNo(i);
            partitionGroup.setElectType(TopicPartitionGroup.ElectType.valueOf(topic.getElectType()).type());
            if(j<surplus){
                partitionGroup.setPartitionSet(new HashSet<>(partitions.subList(index,index+step+1)));
                index = index+step+1;
            }else{
                partitionGroup.setPartitionSet(new HashSet<>(partitions.subList(index,index+step)));
                index = index+step;
            }
            partitionGroup.setPartitions(Arrays.toString(partitionGroup.getPartitionSet().toArray()));
            partitionGroups.add(partitionGroup);
        }
        //
        //每个paritiongroup分配broker及指定推荐leader
        for(int k=0; k<partitionGroups.size();k++){
            TopicPartitionGroup partitionGroup = partitionGroups.get(k);
            for(int j=k;j<topic.getReplica()+k;j++){
                Broker broker = brokers.get(j%brokers.size());
                PartitionGroupReplica replica = new PartitionGroupReplica();
                replica.setGroupNo(partitionGroup.getGroupNo());
                replica.setNamespace(partitionGroup.getNamespace());
                replica.setTopic(partitionGroup.getTopic());
                replica.setBrokerId(Long.valueOf(broker.getId()).intValue());
                if(partitionGroup.getElectType().equals(TopicPartitionGroup.ElectType.fix.type())){
                    if(j==0){
                        replica.setRole(PartitionGroupReplica.ROLE_MASTER);
                    }
                    else {
                        replica.setRole(PartitionGroupReplica.ROLE_DYNAMIC);
                    }
                }else{
                    replica.setRole(PartitionGroupReplica.ROLE_DYNAMIC);
                }
                partitionGroup.getReplicaGroups().add(replica);
                if ((j-k)==brokers.size()){
                    break;
                }
            }
            partitionGroup.setRecLeader(Integer.valueOf(String.valueOf(brokers.get(k).getId())));
        }
        return partitionGroups;
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<QTopic> query) {
        if (query == null) {
            return PageResult.empty();
        }
        // TODO 方法不对
//        return topicNameServerService.findUnsubscribedByQuery(query);
        return topicNameServerService.search(query);
    }

    @Override
    public PageResult<AppUnsubscribedTopic> findAppUnsubscribedByQuery(QPageQuery<QTopic> query) {
        if (query == null) {
            return PageResult.empty();
        }
        if (query.getQuery() == null || query.getQuery().getSubscribeType() == null || query.getQuery().getApp() == null
                || query.getQuery().getApp().getCode() == null) {
            throw new ServiceException(ServiceException.BAD_REQUEST, "bad QTopic query argument.");
        }

        PageResult<Topic> topicResult;
        //consumer do not filter by app, because it can be expand by subscribe group property
        if (query.getQuery().getSubscribeType() == Consumer.CONSUMER_TYPE) {
            try {
                topicResult = topicNameServerService.search(query);
            } catch (Exception e) {
                logger.error("", e);
                throw new ServiceException(ServiceException.NAMESERVER_RPC_ERROR, "query topic by name server error.", e);
            }
        } else {
            // TODO 方法不对
//            topicResult = topicNameServerService.findUnsubscribedByQuery(query);
            topicResult = topicNameServerService.search(query);
        }

        if (NullUtil.isEmpty(topicResult.getResult())) {
            return PageResult.empty();
        }

        return new PageResult(topicResult.getPagination(), topicResult.getResult().stream().map(topic -> {
            AppUnsubscribedTopic appUnsubscribedTopic = new AppUnsubscribedTopic(topic);
            appUnsubscribedTopic.setAppCode(query.getQuery().getApp().getCode());
            appUnsubscribedTopic.setSubscribeType(query.getQuery().getSubscribeType());

            if (query.getQuery().getSubscribeType() == Consumer.CONSUMER_TYPE && StringUtils.isNotBlank(query.getQuery().getApp().getCode())) {
                //find consumer list by topic and app refer, then set showDefaultSubscribeGroup property
                try {
                    Consumer consumer = consumerNameServerService.findByTopicAndApp(topic.getCode(), topic.getNamespace().getCode(), query.getQuery().getApp().getCode());
                    appUnsubscribedTopic.setSubscribeGroupExist(consumer != null);
                } catch (Exception e) {
                    logger.error("can not find consumer list by topic and app refer.", e);
                    appUnsubscribedTopic.setSubscribeGroupExist(Boolean.TRUE);
                }
            }
            return appUnsubscribedTopic;
        }).collect(Collectors.toList()));
    }

    @Override
    public Topic findById(String s) throws Exception {
        return topicNameServerService.findById(s);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int delete(Topic model) throws Exception {
        //validate topic related producers and consumers
        Preconditions.checkArgument(NullUtil.isEmpty(producerNameServerService.findByTopic(model.getCode(), model.getNamespace().getCode())),
                String.format("topic %s exists related producers", CodeConverter.convertTopic(model.getNamespace(), model).getFullName()));
        Preconditions.checkArgument(NullUtil.isEmpty(consumerNameServerService.findByTopic(model.getCode(), model.getNamespace().getCode())),
                String.format("topic %s exists related consumers", CodeConverter.convertTopic(model.getNamespace(), model).getFullName()));
        Preconditions.checkArgument(NullUtil.isEmpty(partitionGroupServerService.findByTopic(model.getCode(), model.getNamespace().getCode())),
                String.format("topic %s exists related partitionGroup", CodeConverter.convertTopic(model.getNamespace(), model).getFullName()));

        // TODO 需要处理
//        Preconditions.checkArgument(NullUtil.isEmpty(replicaServerService.findByTopicAndGroup(model.getCode(), model.getNamespace().getCode()))),
//                String.format("topic %s exists related partitions", CodeConverter.convertTopic(model.getNamespace(), model).getFullName());
        //delete related partition groups
        try {
//            List<TopicPartitionGroup> groups = partitionGroupServerService.findByTopic(model.getCode(), model.getNamespace().getCode());
//            if (NullUtil.isNotEmpty(groups)) {
//                groups.forEach(g -> {
//                    try {
//                        partitionGroupServerService.delete(g);
//                    } catch (Exception e) {
//                        String msg = "delete topic related partition groups error.";
//                        logger.error(msg, e);
//                        throw new ServiceException(INTERNAL_SERVER_ERROR, msg, e);
//                    }
//                });
//            }
//            //delete related partition group replica
//            List<PartitionGroupReplica> replicas = replicaServerService.findByQuery(new QPartitionGroupReplica(model, model.getNamespace()));
//            if (NullUtil.isNotEmpty(replicas)) {
//                replicas.forEach(r -> {
//                    try {
//                        replicaServerService.delete(r);
//                    } catch (Exception e) {
//                        String msg = "delete topic related partition group replicas error.";
//                        logger.error(msg, e);
//                        throw new ServiceException(INTERNAL_SERVER_ERROR, msg, e);
//                    }
//                });
//            }
            //delete topic
            return topicNameServerService.removeTopic(model);
        } catch (Exception e) {
            String errorMsg = "delete topic error.";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg, e);//回滚
        }

    }

    @Override
    public int add(Topic model) throws Exception {
        return 0;
    }

    @Override
    public int update(Topic model) throws Exception {
        return topicNameServerService.update(model);
    }

    @Override
    public Topic findByCode(String namespaceCode, String code) {
        if (namespaceCode == null) {
            namespaceCode = Namespace.DEFAULT_NAMESPACE_CODE;
        }
        return topicNameServerService.findByCode(namespaceCode, code);
    }

    @Override
    public List<TopicName> findTopic(String brokerId) throws Exception {
        List<PartitionGroupReplica> replicas=replicaServerService.findPartitionGroupReplica(Integer.parseInt(brokerId));
        Set<TopicName> topicCodes=new HashSet<>();
        replicas.stream().forEach(r->{
            TopicName tn= TopicName.parse(r.getTopic().getCode(),r.getNamespace().getCode());
            tn.getFullName();
            topicCodes.add(tn);
        });
        return Lists.newArrayList(topicCodes);
    }

    @Override
    public PageResult<Topic> search(QPageQuery<QTopic> query) {
        return topicNameServerService.search(query);
    }

    @Override
    public Set<String> findAppsByTopic(String namespace, String topicCode) {
        Set<String> apps = new TreeSet<>();

        List<org.joyqueue.model.domain.Producer> producers = null;
        try {
            producers = producerNameServerService.findByTopic(topicCode, namespace);
        } catch (Exception e) {
            logger.error("find producer by topic error. ", e);
        }

        if (NullUtil.isNotEmpty(producers)) {
            producers.forEach(p -> apps.add(p.getApp().getCode()));
        }

        List<Consumer> consumers;
        try {
            consumers = consumerNameServerService.findByTopic(topicCode, namespace);
        } catch (Exception e) {
            logger.error("find consumer by topic error. ", e);
            return apps;
        }

        if (CollectionUtils.isNotEmpty(consumers)) {
            consumers.forEach(c -> apps.add(c.getApp().getCode()));
        }

        return apps;
    }
}
