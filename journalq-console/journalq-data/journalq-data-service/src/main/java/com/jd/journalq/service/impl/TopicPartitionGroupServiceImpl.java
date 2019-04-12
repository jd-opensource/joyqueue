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
package com.jd.journalq.service.impl;

import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.exception.ServiceException;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.domain.Topic;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.model.query.QPartitionGroupReplica;
import com.jd.journalq.model.query.QTopicPartitionGroup;
import com.jd.journalq.service.TopicPartitionGroupService;
import com.jd.journalq.nsr.PartitionGroupServerService;
import com.jd.journalq.nsr.ReplicaServerService;
import com.jd.journalq.nsr.TopicNameServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.jd.journalq.exception.ServiceException.IGNITE_RPC_ERROR;
import static com.jd.journalq.exception.ServiceException.INTERNAL_SERVER_ERROR;

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

//    @Override
//    public TopicPartitionGroup findById(long id) {
//        TopicPartitionGroup group = super.findById(id);
//        QPartitionGroupReplica qTopicPartitionGroup = new QPartitionGroupReplica();
//        qTopicPartitionGroup.setTopic(group.getTopic());
//        qTopicPartitionGroup.setNamespace(group.getNamespace());
//        qTopicPartitionGroup.setGroupNo(group.getGroupNo());
//        try {
//            group.setReplicaGroups(new TreeSet<>(replicaServerService.findByQuery(qTopicPartitionGroup)));
//        } catch (Exception e) {
//            throw new ServiceException(IGNITE_RPC_ERROR,e.getMessage());
//        }
//        return group;
//    }

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

        int currentPartitions = topic.getPartitions();
        topic.setPartitions(topic.getPartitions()+Integer.valueOf(model.getPartitionCount()));
        Set<Integer> partitions = Arrays.stream(topicPartitionGroup.getPartitions().substring(1,topicPartitionGroup.getPartitions().length()-1).split(",")).map(m->Integer.valueOf(m.trim())).collect(Collectors.toSet());

        if (model.getPartitionCount()<=0) {
            throw new ServiceException(INTERNAL_SERVER_ERROR, "数据异常");
        }
        for(int i =currentPartitions;i<topic.getPartitions();i++){
            partitions.add(i);
        }
        model.setPartitions(Arrays.toString(partitions.toArray()));
        model.setReplicaGroups(topicPartitionGroup.getReplicaGroups());
        try {
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
        Set<Integer> partitions = Arrays.stream(topicPartitionGroup.getPartitions().substring(1,topicPartitionGroup.getPartitions().length()-1).split(",")).map(m->Integer.valueOf(m.trim())).collect(Collectors.toSet());
        //如果缩减数小于，已有partition数，则更改失败
        if (model.getPartitionCount()>= partitions.size()) {
            throw new ServiceException(INTERNAL_SERVER_ERROR, "数据异常");
        }
        for(int i=currentPartitions-1;i>topic.getPartitions()-1;i--){
            partitions.remove(i);
        }
        model.setPartitions(Arrays.toString(partitions.toArray()));
        model.setReplicaGroups(topicPartitionGroup.getReplicaGroups());
        try {
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
