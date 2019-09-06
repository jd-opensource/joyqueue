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
package io.chubao.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.chubao.joyqueue.convert.CodeConverter;
import io.chubao.joyqueue.convert.NsrTopicConverter;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.domain.PartitionGroupMaster;
import io.chubao.joyqueue.model.domain.PartitionGroupReplica;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QTopic;
import io.chubao.joyqueue.nsr.NameServerBase;
import io.chubao.joyqueue.nsr.TopicNameServerService;
import io.chubao.joyqueue.nsr.model.TopicQuery;
import io.chubao.joyqueue.util.NullUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static io.chubao.joyqueue.exception.ServiceException.NAMESERVER_RPC_ERROR;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("topicNameServerService")
public class TopicNameServerServiceImpl extends NameServerBase implements TopicNameServerService {

    public static final String ADD_TOPIC="/topic/add";
    public static final String REMOVE_TOPIC="/topic/remove";
    public static final String ADD_PARTITION_GROUP="/topic/addPartitionGroup";
    public static final String REMOVE_PARTITION_GROUP="/topic/removePartitionGroup";
    public static final String UPDATE_PARTITION_GROUP="/topic/updatePartitionGroup";
    public static final String LEADER_CHANGE="/topic/leaderChange";
    public static final String FIND_PARTITION_GROUP_MASTER="/topic/getPartitionGroup";
    public static final String GETBYID_TOPIC="/topic/getById";
    public static final String UPDATE_TOPIC="/topic/update";
    public static final String UNSUB_TOPIC="/topic/findUnsubscribedByQuery";
    public static final String SEARCH_TOPIC="/topic/search";
    public static final String GETBYCODE_TOPIC="/topic/getByCode";

    private NsrTopicConverter nsrTopicConverter = new NsrTopicConverter();

    /**
     * 添加主题
     * @param
     * @param topic
     * @param partitionGroups
     * @throws Exception
     */
    @Override
    public String addTopic(Topic topic, List<TopicPartitionGroup> partitionGroups) {
        JSONObject request = new JSONObject();
        io.chubao.joyqueue.domain.Topic nsrTopic = new io.chubao.joyqueue.domain.Topic();
        //数据组装
        nsrTopic.setName(CodeConverter.convertTopic(topic.getNamespace(),topic));
        nsrTopic.setType(io.chubao.joyqueue.domain.Topic.Type.valueOf((byte)topic.getType()));
        nsrTopic.setPartitions((short)topic.getPartitions());
        List<PartitionGroup> nsrPartitionGroups = new ArrayList<>(partitionGroups.size());
        for(TopicPartitionGroup group : partitionGroups){
            PartitionGroup partitionGroup = new PartitionGroup();
            partitionGroup.setPartitions(Arrays.stream(group.getPartitions().substring(1,group.getPartitions().length()-1).split(",")).map(s->Short.parseShort(s.trim())).collect(Collectors.toSet()));
            partitionGroup.setGroup(group.getGroupNo());
            partitionGroup.setTopic(nsrTopic.getName());
            partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(group.getElectType().intValue()));
            Set<Integer> replicaGroups = new TreeSet<>();
            Set<Integer> learners = new TreeSet<>();
            int leader = -1;
            for(PartitionGroupReplica replica : group.getReplicaGroups()){
                replicaGroups.add(replica.getBrokerId());
                if(replica.getRole()==PartitionGroupReplica.ROLE_LEARNER)learners.add(replica.getBrokerId());
                else if(replica.getRole()==PartitionGroupReplica.ROLE_MASTER)leader = replica.getBrokerId();
            }
            partitionGroup.setReplicas(replicaGroups);
            partitionGroup.setLearners(learners);
            partitionGroup.setLeader(leader);
            partitionGroup.setRecLeader(group.getRecLeader());
            nsrPartitionGroups.add(partitionGroup);
        }
        topic.setId(nsrTopic.getName().getFullName());
        request.put("topic", JSON.toJSONString(nsrTopic));
        request.put("partitionGroups", JSON.toJSONString(nsrPartitionGroups));
        return postWithLog(ADD_TOPIC, request, OperLog.Type.TOPIC.value(),OperLog.OperType.ADD.value(),nsrTopic.getName().getCode());
    }
    /**
     * 删除主题
     * @param
     * @param topic
     * @throws Exception
     */
    @Override
    public int removeTopic(Topic topic) {
        io.chubao.joyqueue.domain.Topic nsrTopic = new io.chubao.joyqueue.domain.Topic();
        nsrTopic.setName(CodeConverter.convertTopic(topic.getNamespace(),topic));
        nsrTopic.setType(io.chubao.joyqueue.domain.Topic.Type.valueOf((byte)topic.getType()));
        nsrTopic.setPartitions((short)topic.getPartitions());
        String result =  postWithLog(REMOVE_TOPIC, nsrTopic,OperLog.Type.TOPIC.value(),OperLog.OperType.DELETE.value(),nsrTopic.getName().getCode());
        return isSuccess(result);
    }
    /**
     * 添加partitionGroup
     * @throws Exception
     */
    @Override
    public String addPartitionGroup(TopicPartitionGroup group) {
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setPartitions(Arrays.stream(group.getPartitions().substring(1,group.getPartitions().length()-1).split(",")).map(s->Short.parseShort(s.trim())).collect(Collectors.toSet()));
        partitionGroup.setGroup(group.getGroupNo());
        partitionGroup.setTopic(CodeConverter.convertTopic(group.getNamespace(),group.getTopic()));
        partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(group.getElectType().intValue()));
        Set<Integer> replicaGroups = new TreeSet<>();
        Set<Integer> learners = new TreeSet<>();
        int leader = -1;
        for(PartitionGroupReplica replica : group.getReplicaGroups()){
            replicaGroups.add(replica.getBrokerId());
            if(replica.getRole()==PartitionGroupReplica.ROLE_LEARNER)learners.add(replica.getBrokerId());
            else if(replica.getRole()==PartitionGroupReplica.ROLE_MASTER)leader = replica.getBrokerId();
        }
        partitionGroup.setReplicas(replicaGroups);
        partitionGroup.setLearners(learners);
        partitionGroup.setLeader(leader);
        partitionGroup.setRecLeader(group.getRecLeader());
        return postWithLog(ADD_PARTITION_GROUP, partitionGroup,OperLog.Type.GROUP.value(),OperLog.OperType.ADD.value(),group.getTopic().getCode());
    }
    /**
     * 移除partitionGroup
     * @throws Exception
     */
    @Override
    public String removePartitionGroup(TopicPartitionGroup group) {
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setGroup(group.getGroupNo());
        Set<Short> partitions = Arrays.stream(group.getPartitions().substring(1, group.getPartitions().length()-1).split(",")).map(m-> Short.parseShort(m.trim())).collect(Collectors.toSet());
        partitionGroup.setPartitions(partitions);
        partitionGroup.setTopic(CodeConverter.convertTopic(group.getNamespace(),group.getTopic()));
        return postWithLog(REMOVE_PARTITION_GROUP, partitionGroup,OperLog.Type.GROUP.value(),OperLog.OperType.DELETE.value(),group.getTopic().getCode());
    }

    /**
     * 更新 partitionGroup
     * @param group
     * @return
     * @throws Exception
     */
    @Override
    public List<Integer> updatePartitionGroup(TopicPartitionGroup group) {
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setPartitions(Arrays.stream(group.getPartitions().substring(1,group.getPartitions().length()-1).split(",")).map(s->Short.parseShort(s.trim())).collect(Collectors.toSet()));
        partitionGroup.setGroup(group.getGroupNo());
        partitionGroup.setTopic(CodeConverter.convertTopic(group.getNamespace(),group.getTopic()));
        partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(group.getElectType().intValue()));
        Set<Integer> replicaGroups = new TreeSet<>();
        Set<Integer> learners = new TreeSet<>();
        int leader = -1;
        for(PartitionGroupReplica replica : group.getReplicaGroups()){
            replicaGroups.add(replica.getBrokerId());
            if(replica.getRole()==PartitionGroupReplica.ROLE_LEARNER)learners.add(replica.getBrokerId());
            else if(replica.getRole()==PartitionGroupReplica.ROLE_MASTER)leader = replica.getBrokerId();
        };
        partitionGroup.setReplicas(replicaGroups);
        partitionGroup.setLearners(learners);
        partitionGroup.setLeader(leader);
        partitionGroup.setRecLeader(group.getRecLeader());
        List<Integer> outSyncBrokers = JSONArray.parseArray(
                postWithLog(UPDATE_PARTITION_GROUP, partitionGroup,OperLog.Type.GROUP.value(),OperLog.OperType.UPDATE.value(),group.getTopic().getCode()),Integer.class);
        return outSyncBrokers;
    }

    /**
     * leader指定
     * @param group
     */
    @Override
    public int leaderChange(TopicPartitionGroup group) {
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setGroup(group.getGroupNo());
        partitionGroup.setPartitions(Arrays.stream(group.getPartitions().substring(1,group.getPartitions().length()-1).split(",")).map(s->Short.parseShort(s.trim())).collect(Collectors.toSet()));
        partitionGroup.setTopic(CodeConverter.convertTopic(group.getNamespace(),group.getTopic()));
        partitionGroup.setLeader(group.getLeader());
        partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(group.getElectType().intValue()));
        partitionGroup.setOutSyncReplicas(group.getOutSyncReplicas());
        String result =  postWithLog(LEADER_CHANGE, partitionGroup,OperLog.Type.TOPIC.value(),OperLog.OperType.UPDATE.value(),group.getTopic().getCode());
        return isSuccess(result);
    }
    @Override
    public List<PartitionGroup> findPartitionGroupMaster(List<TopicPartitionGroup> topicPartitionGroups) {
        if(NullUtil.isEmpty(topicPartitionGroups)) {
            return null;
        }
        PartitionGroupMaster partitionGroupMaster = new PartitionGroupMaster();
        partitionGroupMaster.setGroups(new ArrayList<>(topicPartitionGroups.size()));
        TopicPartitionGroup topicPartitionGroup = topicPartitionGroups.get(0);
        partitionGroupMaster.setNamespace(null==topicPartitionGroup.getNamespace()?TopicName.DEFAULT_NAMESPACE:topicPartitionGroup.getNamespace().getCode());
        partitionGroupMaster.setTopic(topicPartitionGroup.getTopic().getCode());
        partitionGroupMaster.getGroups().add(topicPartitionGroup.getGroupNo());
        for(int i=1; i<topicPartitionGroups.size(); i++){
            partitionGroupMaster.getGroups().add(topicPartitionGroups.get(i).getGroupNo());
        }
        return JSON.parseArray(post(FIND_PARTITION_GROUP_MASTER, partitionGroupMaster), PartitionGroup.class);
    }

    @Override
    public int delete(Topic model) throws Exception {
        io.chubao.joyqueue.domain.Topic nsrTopic = nsrTopicConverter.convert(model);
       String result = postWithLog(REMOVE_TOPIC,nsrTopic,OperLog.Type.TOPIC.value(),OperLog.OperType.DELETE.value(),nsrTopic.getName().getCode());
       return isSuccess(result);
    }

    @Override
    public int add(Topic model) throws Exception {
        try {
            throw new RuntimeException("请使用addTopic接口");
        } catch (Exception e) {
            throw new ServiceException(NAMESERVER_RPC_ERROR,e.getMessage());
        }
    }

    @Override
    public int update(Topic model) throws Exception {
        io.chubao.joyqueue.domain.Topic nsrTopic = nsrTopicConverter.convert(model);
        String result = postWithLog(UPDATE_TOPIC,nsrTopic,OperLog.Type.TOPIC.value(),OperLog.OperType.UPDATE.value(),nsrTopic.getName().getCode());
        return isSuccess(result);
    }

    @Override
    public PageResult<Topic> search(QPageQuery<QTopic> query) {
        try {
            TopicQuery topicQuery = topicQueryConvert(query.getQuery());
            String result =  post(UNSUB_TOPIC,new QPageQuery<>(query.getPagination(),topicQuery));
            PageResult<io.chubao.joyqueue.domain.Topic> pageResult = JSON.parseObject(result,new TypeReference<PageResult<io.chubao.joyqueue.domain.Topic>>(){});
            if (pageResult == null || pageResult.getResult() == null) return PageResult.empty();
            return new PageResult<>(pageResult.getPagination(),pageResult.getResult().stream().map(topic -> nsrTopicConverter.revert(topic)).collect(Collectors.toList()));
        } catch (Exception e) {
            throw new ServiceException(NAMESERVER_RPC_ERROR, e.getMessage());
        }
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<QTopic> query) {
        try {
            TopicQuery topicQuery = topicQueryConvert(query.getQuery());
            String result =  post(UNSUB_TOPIC,new QPageQuery<>(query.getPagination(),topicQuery));
            PageResult<io.chubao.joyqueue.domain.Topic> pageResult = JSON.parseObject(result,new TypeReference<PageResult<io.chubao.joyqueue.domain.Topic>>(){});
            if (pageResult == null || pageResult.getResult() == null) return PageResult.empty();
            return new PageResult<>(pageResult.getPagination(),pageResult.getResult().stream().map(topic -> nsrTopicConverter.revert(topic)).collect(Collectors.toList()));
        } catch (Exception e) {
            throw new ServiceException(NAMESERVER_RPC_ERROR, e.getMessage());
        }
    }

    @Override
    public Topic findByCode(String namespaceCode, String code) {
        try {
            TopicQuery topicQuery = new TopicQuery();
            topicQuery.setNamespace(namespaceCode);
            topicQuery.setCode(code);
            io.chubao.joyqueue.domain.Topic nsrToic= JSON.parseObject(post(GETBYCODE_TOPIC, topicQuery), io.chubao.joyqueue.domain.Topic.class);
            return nsrTopicConverter.revert(nsrToic);
        } catch (Exception e) {
            throw new ServiceException(NAMESERVER_RPC_ERROR,e.getMessage());
        }
    }

    @Override
    public Topic findById(String id) {
        try {
            io.chubao.joyqueue.domain.Topic nsrToic= JSON.parseObject(post(GETBYID_TOPIC,id), io.chubao.joyqueue.domain.Topic.class);
            return nsrTopicConverter.revert(nsrToic);
        } catch (Exception e) {
            throw new ServiceException(NAMESERVER_RPC_ERROR,e.getMessage());
        }
    }
    private TopicQuery topicQueryConvert(QTopic qTopic){
        if ( qTopic == null) return null;
        TopicQuery topicQuery = new TopicQuery();
        if (qTopic.getType() >= 0) {
            topicQuery.setType(io.chubao.joyqueue.domain.Topic.Type.valueOf((byte) qTopic.getType()).code());
        }
        if (qTopic.getNamespace() != null) {
            topicQuery.setNamespace(qTopic.getNamespace());
        }
        if (qTopic.getApp() != null) {
            topicQuery.setApp(qTopic.getApp().getCode());
        }
        if (qTopic.getKeyword() != null) {
            topicQuery.setKeyword(qTopic.getKeyword());
        }
        if (qTopic.getCode()!= null) {
            topicQuery.setCode(qTopic.getCode());
        }
        if (qTopic.getSubscribeType() != null) {
            topicQuery.setSubscribeType(qTopic.getSubscribeType());
        }
        return topicQuery;
    }
}
