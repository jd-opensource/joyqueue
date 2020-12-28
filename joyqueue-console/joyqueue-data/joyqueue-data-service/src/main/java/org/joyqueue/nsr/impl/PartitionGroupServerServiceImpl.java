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
package org.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import org.joyqueue.convert.NsrPartitionGroupConverter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Replica;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.nsr.NameServerBase;
import org.joyqueue.nsr.PartitionGroupServerService;
import org.joyqueue.nsr.model.PartitionGroupQuery;
import org.joyqueue.nsr.model.ReplicaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
@Service("partitionGroupServerService")
public class PartitionGroupServerServiceImpl extends NameServerBase implements PartitionGroupServerService {
    public static final String ADD_PARTITIONGROUP="/partitiongroup/add";
    public static final String REMOVE_PARTITIONGROUP="/partitiongroup/remove";
    public static final String UPDATE_PARTITIONGROUP="/partitiongroup/update";
    public static final String GETBYID_PARTITIONGROUP="/partitiongroup/getById";
    public static final String GETBYTOPIC_PARTITIONGROUP="/partitiongroup/getByTopic";
    public static final String GETBYTOPICANDGROUP_PARTITIONGROUP="/partitiongroup/getByTopicAndGroup";
    public static final String POSTBY_REPLICA_BROKER = "/replica/getByBroker";

    private NsrPartitionGroupConverter nsrPartitionGroupConverter = new NsrPartitionGroupConverter();

    @Override
    public TopicPartitionGroup findById(String s) throws Exception {
        String result = post(GETBYID_PARTITIONGROUP,s);
        PartitionGroup partitionGroup = JSON.parseObject(result,PartitionGroup.class);
        return nsrPartitionGroupConverter.revert(partitionGroup);
    }

    @Override
    public int delete(TopicPartitionGroup model) throws Exception {
        PartitionGroup partitionGroup = nsrPartitionGroupConverter.convert(model);
        String result = postWithLog(REMOVE_PARTITIONGROUP,partitionGroup, OperLog.Type.PARTITION_GROUP.value(),OperLog.OperType.DELETE.value(),partitionGroup.getTopic().getCode());
        return isSuccess(result);
    }

    @Override
    public int add(TopicPartitionGroup model) throws Exception {
        PartitionGroup partitionGroup = nsrPartitionGroupConverter.convert(model);
        String result = postWithLog(ADD_PARTITIONGROUP,partitionGroup,OperLog.Type.PARTITION_GROUP.value(),OperLog.OperType.ADD.value(),partitionGroup.getTopic().getCode());
        return isSuccess(result);
    }

    @Override
    public int update(TopicPartitionGroup model) throws Exception {
        PartitionGroup partitionGroup = nsrPartitionGroupConverter.convert(model);
        String result = postWithLog(UPDATE_PARTITIONGROUP,partitionGroup,OperLog.Type.PARTITION_GROUP.value(),OperLog.OperType.UPDATE.value(),partitionGroup.getTopic().getCode());
        return isSuccess(result);
    }

    @Override
    public TopicPartitionGroup findByTopicAndGroup(String namespace, String topic, Integer groupNo) {
        PartitionGroupQuery query = new PartitionGroupQuery();
        query.setTopic(topic);
        query.setNamespace(namespace);
        query.setGroup(groupNo);

        String result = post(GETBYTOPICANDGROUP_PARTITIONGROUP, query);
        PartitionGroup partitionGroup = JSON.parseObject(result, PartitionGroup.class);
        return nsrPartitionGroupConverter.revert(partitionGroup);
    }

    @Override
    public List<TopicPartitionGroup> findByTopic(String topic, String namespace) {
        PartitionGroupQuery query = new PartitionGroupQuery();
        query.setTopic(topic);
        query.setNamespace(namespace);

        String result = post(GETBYTOPIC_PARTITIONGROUP, query);
        List<PartitionGroup> partitionGroups = JSON.parseArray(result, PartitionGroup.class);
        return partitionGroups.stream().map(partitionGroup -> nsrPartitionGroupConverter.revert(partitionGroup)).collect(Collectors.toList());
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        ReplicaQuery replicaQuery = new ReplicaQuery();
        replicaQuery.setBrokerId(brokerId);
        String result = post(POSTBY_REPLICA_BROKER, replicaQuery);
        return JSON.parseArray(result, Replica.class);
    }
}
