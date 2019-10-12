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
package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.PartitionGroupReplica;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QPartitionGroupReplica;
import io.chubao.joyqueue.model.query.QTopicPartitionGroup;
import io.chubao.joyqueue.service.TopicPartitionGroupService;
import io.chubao.joyqueue.nsr.PartitionGroupServerService;
import io.chubao.joyqueue.nsr.ReplicaServerService;
import io.chubao.joyqueue.nsr.TopicNameServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static io.chubao.joyqueue.exception.ServiceException.IGNITE_RPC_ERROR;
import static io.chubao.joyqueue.exception.ServiceException.INTERNAL_SERVER_ERROR;
import static io.chubao.joyqueue.exception.ServiceException.BAD_REQUEST;

/**
 * topic partition group service implement
 * Created by chenyanying3 on 2018-10-18
 */
@Service("topicPartitionGroupService")
public class TopicPartitionGroupServiceImpl  implements TopicPartitionGroupService {
    private final Logger logger = LoggerFactory.getLogger(TopicPartitionGroupServiceImpl.class);

    private final String PARTITIONS_SPLIT=",";
    private final String PARTITIONS_PREFIX="[";
    private final String PARTITIONS_SUFFIX="]";
    @Autowired
    private TopicNameServerService topicNameServerService;

    @Autowired
    protected PartitionGroupServerService partitionGroupServerService;

    @Autowired
    private ReplicaServerService replicaServerService;


    @Override
    public TopicPartitionGroup findByTopicAndGroup(String namespace, String topic, Integer groupNo) {

        TopicPartitionGroup group = null;
        try {
            group = partitionGroupServerService.findByQuery(new QTopicPartitionGroup(new Topic(topic),new Namespace(namespace),groupNo)).get(0);
        } catch (Exception e) {
            logger.error("findByQuery error",e);
            throw new RuntimeException(e);
        }

        QPartitionGroupReplica qTopicPartitionGroup = new QPartitionGroupReplica();
        qTopicPartitionGroup.setTopic(group.getTopic());
        qTopicPartitionGroup.setNamespace(group.getNamespace());
        qTopicPartitionGroup.setGroupNo(group.getGroupNo());
        try {
            List<PartitionGroupReplica> topicPartitionGroups = replicaServerService.findByQuery(qTopicPartitionGroup);
            if (topicPartitionGroups != null ) {
                group.setReplicaGroups(new TreeSet<>(topicPartitionGroups));
            }
        } catch (Exception e) {
            logger.error("exception",e);
            throw new ServiceException(IGNITE_RPC_ERROR,e.getMessage());
        }
        return group;
    }

    @Override
    public List<TopicPartitionGroup> findByTopic(Namespace namespace, Topic topic) {
        try {
            //Find database
            List<TopicPartitionGroup> topicPartitionGroups = partitionGroupServerService.findByQuery(new QTopicPartitionGroup(topic,namespace));
            if (topicPartitionGroups == null || topicPartitionGroups.isEmpty()) {
                return Collections.emptyList();
            }
            return topicPartitionGroups;
        } catch (Exception e) {
            String errorMsg = "新添加partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, errorMsg);
        }
    }
    // 支持指定 partition or partition数量，递增生成partition
    @Override
    public int add(TopicPartitionGroup model) {
        Topic topic = topicNameServerService.findByCode(model.getNamespace().getCode(),model.getTopic().getCode());

        List<TopicPartitionGroup> groups = null;
        try {
            groups = partitionGroupServerService.findByQuery(new QTopicPartitionGroup(new Topic(model.getTopic().getCode())));
        } catch (Exception e) {
            logger.error("partitionGroupServerService.findByQuery",e);
            throw new ServiceException(IGNITE_RPC_ERROR,e.getMessage());
        }
        int currentPartitions = topic.getPartitions();
        if (groups != null) {
            model.setGroupNo(groups.size());
        } else {
            model.setGroupNo(0);
        }
        // 支持添加指定 partition,用","分割
        if(model.getPartitions().startsWith(PARTITIONS_PREFIX)&&model.getPartitions().endsWith(PARTITIONS_SUFFIX)){
           String partitionList= model.getPartitions();
           String[] partitions=partitionList.substring(1,partitionList.length()-1).split(PARTITIONS_SPLIT);
           for(String p:partitions){
               model.getPartitionSet().add(Integer.parseInt(p));
           }
        }else {
            topic.setPartitions(topic.getPartitions() + Integer.valueOf(model.getPartitions()));
            for (int i = currentPartitions; i < topic.getPartitions(); i++) {
                model.getPartitionSet().add(i);
            }
        }
        model.setPartitions(Arrays.toString(model.getPartitionSet().toArray()));
        try {
            topicNameServerService.addPartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "新添加partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, errorMsg);//回滚
        }
        return 1;
    }

    /**
     * 新增partiotion
     * @param model
     * @return
     * @throws Exception
     */
    @Override
    public int addPartition(TopicPartitionGroup model) throws Exception {
        Topic topic = topicNameServerService.findByCode(model.getNamespace().getCode(),model.getTopic().getCode());
        TopicPartitionGroup topicPartitionGroup = findByTopicAndGroup(model.getNamespace().getCode(),model.getTopic().getCode(),model.getGroupNo());

        Set<Integer> partitions = Arrays.stream(topicPartitionGroup.getPartitions().substring(1,topicPartitionGroup.getPartitions().length()-1).split(",")).
                map(m->Integer.valueOf(m.trim())).collect(Collectors.toSet());
        if(model.getPartitionCount().startsWith(PARTITIONS_PREFIX)&&model.getPartitions().endsWith(PARTITIONS_SUFFIX)) {
            String partitionList= model.getPartitionCount();
            String[] increPartitions=partitionList.substring(1,partitionList.length()-1).split(PARTITIONS_SPLIT);
            List<TopicPartitionGroup> partitionGroups=findByTopic(model.getNamespace(),model.getTopic());
            // 所有的partition
            Set<Integer> partitionSet=new HashSet();
            for(TopicPartitionGroup pg:partitionGroups) {
                partitionSet.addAll(Arrays.stream(pg.getPartitions().substring(1,
                        pg.getPartitions().length() - 1).split(",")).map(m -> Integer.valueOf(m.trim())).collect(Collectors.toSet()));
            }
            for(String p:increPartitions){
                if(!partitionSet.contains(p)) {
                    partitions.add(Integer.parseInt(p));
                }
            }
        }else {
            int currentPartitions = topic.getPartitions();
            topic.setPartitions(topic.getPartitions()+Integer.valueOf(model.getPartitionCount()));
            if (Integer.valueOf(model.getPartitionCount()) <= 0) {
                throw new ServiceException(INTERNAL_SERVER_ERROR, "数据异常");
            }
            for (int i = currentPartitions; i < topic.getPartitions(); i++) {
                partitions.add(i);
            }
        }
        model.setPartitions(Arrays.toString(partitions.toArray()));
        model.setReplicaGroups(topicPartitionGroup.getReplicaGroups());
        try {
            if (model.getReplicaGroups() == null || model.getReplicaGroups().size() <= 0) {
                throw new ServiceException(BAD_REQUEST, "副本不能为空");
            }
            topicNameServerService.updatePartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "更新partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, errorMsg);//回滚
        }
        return 1;
    }

    /**
     * 减少partition
     * @param model
     * @return
     * @throws Exception
     */
    @Override
    public int removePartition(TopicPartitionGroup model) throws Exception {
        Topic topic = topicNameServerService.findByCode(model.getNamespace().getCode(),model.getTopic().getCode());
        TopicPartitionGroup topicPartitionGroup = findByTopicAndGroup(model.getNamespace().getCode(),model.getTopic().getCode(),model.getGroupNo());

        int currentPartitions = topic.getPartitions();
        topic.setPartitions(topic.getPartitions()-Integer.valueOf(model.getPartitionCount()));
        Set<Integer> partitions = Arrays.stream(topicPartitionGroup.getPartitions().substring(1,topicPartitionGroup.getPartitions().length()-1).split(",")).
                map(m->Integer.valueOf(m.trim())).collect(Collectors.toSet());
        //如果缩减数小于，已有partition数，则更改失败
        if (Integer.valueOf(model.getPartitionCount())>= partitions.size()) {
            throw new ServiceException(INTERNAL_SERVER_ERROR, "数据异常");
        }
        for(int i=currentPartitions-1;i>topic.getPartitions()-1;i--){
            if(!partitions.contains(i)){
                throw new ServiceException(BAD_REQUEST,"请先缩减partition"+i+"所在partitionGroup,否则会导致不连续");
            }
            partitions.remove(i);
        }
        model.setPartitions(Arrays.toString(partitions.toArray()));
        model.setReplicaGroups(topicPartitionGroup.getReplicaGroups());
        try {
            if (model.getReplicaGroups() == null || model.getReplicaGroups().size() <=0) {
                throw new ServiceException(BAD_REQUEST,"副本不能为空");
            }
            topicNameServerService.updatePartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "更新partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, errorMsg);//回滚
        }
        return 1;
    }

    @Override
    public int leaderChange(TopicPartitionGroup model) throws Exception {
        TopicPartitionGroup topicPartitionGroup = findByTopicAndGroup(model.getNamespace().getCode(),model.getTopic().getCode(),model.getGroupNo());
        topicPartitionGroup.setLeader(model.getLeader());
        topicPartitionGroup.setOutSyncReplicas(model.getOutSyncReplicas());
        return topicNameServerService.leaderChange(topicPartitionGroup);
    }

    @Override
    public int update(TopicPartitionGroup model) throws Exception {
        return 0;
    }

    @Override
    public List<TopicPartitionGroup> findByQuery(QTopicPartitionGroup query) throws Exception {
        return partitionGroupServerService.findByQuery(query);
    }

    @Override
    public TopicPartitionGroup findById(String s) throws Exception {
        return partitionGroupServerService.findById(s);
    }

    @Override
    public PageResult<TopicPartitionGroup> findByQuery(QPageQuery<QTopicPartitionGroup> query) throws Exception {
        return partitionGroupServerService.findByQuery(query);
    }

    @Override
    public int delete(TopicPartitionGroup model) {
        Topic topic = topicNameServerService.findByCode(model.getNamespace().getCode(),model.getTopic().getCode());
        int currentPartitions = topic.getPartitions();
        Set<Integer> partitions = Arrays.stream(model.getPartitions().substring(1,model.getPartitions().length()-1).split(",")).
                map(m->Integer.valueOf(m.trim())).collect(Collectors.toSet());
        topic.setPartitions(topic.getPartitions()-partitions.size());
        for(int i=currentPartitions-1;i>topic.getPartitions()-1;i--){
            if(!partitions.contains(i)){
                throw new ServiceException(BAD_REQUEST,"请先删除partition"+i+"所在partitionGroup");
            }
        }
        try {
            topicNameServerService.removePartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "删除partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, errorMsg);//回滚
        }
        return 1;
    }
}
