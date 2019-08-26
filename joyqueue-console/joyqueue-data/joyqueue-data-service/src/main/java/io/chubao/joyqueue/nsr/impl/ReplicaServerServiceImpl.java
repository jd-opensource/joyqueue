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
import io.chubao.joyqueue.convert.NsrReplicaConverter;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.domain.PartitionGroupReplica;
import io.chubao.joyqueue.nsr.NameServerBase;
import io.chubao.joyqueue.nsr.ReplicaServerService;
import io.chubao.joyqueue.nsr.model.ReplicaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
@Service("replicaServerService")
public class ReplicaServerServiceImpl extends NameServerBase implements ReplicaServerService {
    public static final String ADD_REPLICA = "/replica/add";
    public static final String REMOVE_REPLICA = "/replica/remove";
    public static final String UPDATE_REPLICA = "/replica/update";
    public static final String GETBYID_REPLICA = "/replica/getById";
    public static final String GETBYTOPIC_REPLICA = "/replica/getByTopic";
    public static final String GETBYTOPICANDGROUP_REPLICA = "/replica/getByTopicAndGroup";
    private NsrReplicaConverter nsrReplicaConverter = new NsrReplicaConverter();

    @Override
    public PartitionGroupReplica findById(String id) throws Exception {
        String result = post(GETBYID_REPLICA,id);
        Replica replica = JSON.parseObject(result,Replica.class);
        return nsrReplicaConverter.revert(replica);
    }

    @Override
    public List<PartitionGroupReplica> findByTopic(String topic, String namespace) {
        ReplicaQuery replicaQuery = new ReplicaQuery();
        replicaQuery.setTopic(topic);
        replicaQuery.setNamespace(namespace);

        String result = post(GETBYTOPIC_REPLICA, replicaQuery);
        List<Replica> replicas = JSON.parseArray(result,Replica.class);
        return replicas.stream().map(replica -> nsrReplicaConverter.revert(replica)).collect(Collectors.toList());
    }

    @Override
    public List<PartitionGroupReplica> findByTopicAndGroup(String topic, String namespace, int group) {
        ReplicaQuery replicaQuery = new ReplicaQuery();
        replicaQuery.setTopic(topic);
        replicaQuery.setNamespace(namespace);
        replicaQuery.setGroup(group);

        String result = post(GETBYTOPICANDGROUP_REPLICA, replicaQuery);
        List<Replica> replicas = JSON.parseArray(result,Replica.class);
        return replicas.stream().map(replica -> nsrReplicaConverter.revert(replica)).collect(Collectors.toList());
    }

    @Override
    public int delete(PartitionGroupReplica model) throws Exception {
        Replica replica = nsrReplicaConverter.convert(model);
        String result = postWithLog(REMOVE_REPLICA,replica,OperLog.Type.REPLICA.value(),OperLog.OperType.DELETE.value(),replica.getTopic().getCode());
        return isSuccess(result);
    }

    @Override
    public int add(PartitionGroupReplica model) throws Exception {
        Replica replica = nsrReplicaConverter.convert(model);
        String result = postWithLog(ADD_REPLICA,replica,OperLog.Type.REPLICA.value(),OperLog.OperType.ADD.value(),replica.getTopic().getCode());
        return isSuccess(result);
    }

    @Override
    public int update(PartitionGroupReplica model) throws Exception {
        Replica replica = nsrReplicaConverter.convert(model);
        String result = postWithLog(UPDATE_REPLICA,replica,OperLog.Type.REPLICA.value(),OperLog.OperType.UPDATE.value(),replica.getTopic().getCode());
        return isSuccess(result);
    }
}
