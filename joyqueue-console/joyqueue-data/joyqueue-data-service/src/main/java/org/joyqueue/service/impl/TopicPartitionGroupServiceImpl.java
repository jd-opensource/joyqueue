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

import com.alibaba.fastjson.JSON;
import org.joyqueue.exception.ServiceException;
import org.joyqueue.model.domain.Namespace;
import org.joyqueue.model.domain.PartitionGroupReplica;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.model.query.QPartitionGroupReplica;
import org.joyqueue.nsr.PartitionGroupServerService;
import org.joyqueue.nsr.ReplicaServerService;
import org.joyqueue.nsr.TopicNameServerService;
import org.joyqueue.service.TopicPartitionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * topic partition group service implement
 * Created by chenyanying3 on 2018-10-18
 */
@Service("topicPartitionGroupService")
public class TopicPartitionGroupServiceImpl  implements TopicPartitionGroupService {
    private final Logger logger = LoggerFactory.getLogger(TopicPartitionGroupServiceImpl.class);

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
            group = partitionGroupServerService.findByTopicAndGroup(namespace, topic, groupNo);
        } catch (Exception e) {
            logger.error("findByQuery error",e);
            throw new RuntimeException(e);
        }

        QPartitionGroupReplica qTopicPartitionGroup = new QPartitionGroupReplica();
        qTopicPartitionGroup.setTopic(group.getTopic());
        qTopicPartitionGroup.setNamespace(group.getNamespace());
        qTopicPartitionGroup.setGroupNo(group.getGroupNo());
        try {
            List<PartitionGroupReplica> topicPartitionGroups = replicaServerService.findByTopicAndGroup(topic, namespace, groupNo);
            if (topicPartitionGroups != null ) {
                group.setReplicaGroups(new TreeSet<>(topicPartitionGroups));
            }
        } catch (Exception e) {
            logger.error("exception",e);
            throw new ServiceException(ServiceException.NAMESERVER_RPC_ERROR,e.getMessage(), e);
        }
        return group;
    }

    @Override
    public List<TopicPartitionGroup> findByTopic(Namespace namespace, Topic topic) {
        try {
            //Find database
            List<TopicPartitionGroup> topicPartitionGroups = partitionGroupServerService.findByTopic(topic.getCode(), namespace.getCode());
            if (topicPartitionGroups == null || topicPartitionGroups.isEmpty()) {
                return Collections.emptyList();
            }
            topicPartitionGroups.forEach(item -> {
                String partitionStr = item.getPartitions();
                List<Integer> partitions = JSON.parseArray(partitionStr, Integer.class);
                partitions.sort((Integer o1, Integer o2) -> {
                    if (o1 > o2) {
                        return 1;
                    } else if (o1.equals(o2)) {
                        return 0;
                    } else {
                        return -1;
                    }
                });
                item.setPartitions(JSON.toJSONString(partitions));
            });
            return topicPartitionGroups;
        } catch (Exception e) {
            String errorMsg = "查询partitionGroup失败";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg, e);
        }
    }

    @Override
    public List<TopicPartitionGroup> findByTopic(String namespace, String topic) {
        return partitionGroupServerService.findByTopic(namespace, topic);
    }

    @Override
    public int add(TopicPartitionGroup model) {
        Topic topic = topicNameServerService.findByCode(model.getNamespace().getCode(),model.getTopic().getCode());

        List<TopicPartitionGroup> groups = null;
        try {
            groups = partitionGroupServerService.findByTopic(model.getTopic().getCode(), model.getNamespace().getCode());
        } catch (Exception e) {
            logger.error("partitionGroupServerService.findByQuery",e);
            throw new ServiceException(ServiceException.NAMESERVER_RPC_ERROR,e.getMessage(), e);
        }
        int currentPartitions = topic.getPartitions();
        if (groups != null) {
            model.setGroupNo(groups.size());
        } else {
            model.setGroupNo(0);
        }
        topic.setPartitions(topic.getPartitions()+Integer.valueOf(model.getPartitions()));
        for(int i =currentPartitions;i<topic.getPartitions();i++){
            model.getPartitionSet().add(i);
        }
        model.setPartitions(Arrays.toString(model.getPartitionSet().toArray()));

        try {
            topicNameServerService.addPartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "新添加partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg);//回滚
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

        int currentPartitions = topic.getPartitions();
        topic.setPartitions(topic.getPartitions()+Integer.valueOf(model.getPartitionCount()));
        Set<Integer> partitions = Arrays.stream(topicPartitionGroup.getPartitions().substring(1,topicPartitionGroup.getPartitions().length()-1).split(",")).
                map(m->Integer.valueOf(m.trim())).collect(Collectors.toSet());

        if (model.getPartitionCount()<=0) {
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, "数据异常");
        }
        for(int i =currentPartitions;i<topic.getPartitions();i++){
            partitions.add(i);
        }
        model.setPartitions(Arrays.toString(partitions.toArray()));
        model.setReplicaGroups(topicPartitionGroup.getReplicaGroups());
        try {
            if (model.getReplicaGroups() == null || model.getReplicaGroups().size() <=0) {
                throw new ServiceException(ServiceException.BAD_REQUEST,"副本不能为空");
            }
            topicNameServerService.updatePartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "更新partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg);//回滚
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
        if (model.getPartitionCount()>= partitions.size()) {
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, "数据异常");
        }
        for(int i=currentPartitions-1;i>topic.getPartitions()-1;i--){
            if(!partitions.contains(i)){
                throw new ServiceException(ServiceException.BAD_REQUEST,"请先缩减partition"+i+"所在partitionGroup,否则会导致不连续");
            }
            partitions.remove(i);
        }
        model.setPartitions(Arrays.toString(partitions.toArray()));
        model.setReplicaGroups(topicPartitionGroup.getReplicaGroups());
        try {
            if (model.getReplicaGroups() == null || model.getReplicaGroups().size() <=0) {
                throw new ServiceException(ServiceException.BAD_REQUEST,"副本不能为空");
            }
            topicNameServerService.updatePartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "更新partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg);//回滚
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
    public int update(TopicPartitionGroup partitionGroup) throws Exception {
        return 0;
    }

    @Override
    public TopicPartitionGroup findById(String s) throws Exception {
        return partitionGroupServerService.findById(s);
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
                throw new ServiceException(ServiceException.BAD_REQUEST,"请先删除partition"+i+"所在partitionGroup");
            }
        }
        try {
            topicNameServerService.removePartitionGroup(model);
        } catch (Exception e) {
            String errorMsg = "删除partitionGroup，同步NameServer失败";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg);//回滚
        }
        return 1;
    }
}
