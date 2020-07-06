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
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.ignite.dao.PartitionGroupReplicaDao;
import org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica;
import org.joyqueue.nsr.model.ReplicaQuery;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:09 2018/8/13
 */
public class IgnitePartitionGroupReplicaInternalService implements PartitionGroupReplicaInternalService {

    private PartitionGroupReplicaDao replicaDao;

    @Inject
    public IgnitePartitionGroupReplicaInternalService(PartitionGroupReplicaDao replicaDao) {
        this.replicaDao = replicaDao;
    }


    public IgnitePartitionGroupReplica toIgniteModel(Replica model) {
        return new IgnitePartitionGroupReplica(model);
    }

    @Override
    public List<Replica> getByTopic(TopicName topic) {
        return convert(replicaDao.list(new ReplicaQuery(topic.getCode(), topic.getNamespace())));
    }

    @Override
    public List<Replica> getByTopicAndGroup(TopicName topic, int groupNo) {
        return convert(replicaDao.list(new ReplicaQuery(topic.getCode(), topic.getNamespace(), groupNo)));
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        return convert(replicaDao.list(new ReplicaQuery(brokerId)));
    }

    @Override
    public Replica getById(String id) {
        return replicaDao.findById(id);
    }

    @Override
    public List<Replica> getAll() {
        return convert(replicaDao.list(null));
    }

    @Override
    public Replica add(Replica replica) {
        replicaDao.addOrUpdate(toIgniteModel(replica));
        return replica;
    }

    @Override
    public Replica update(Replica replica) {
        replicaDao.addOrUpdate(toIgniteModel(replica));
        return replica;
    }

    @Override
    public void delete(String id) {
        replicaDao.deleteById(id);
    }

    public void addOrUpdate(Replica replica) {
        replicaDao.addOrUpdate(toIgniteModel(replica));
    }

    public void deleteByTopicAndPartitionGroup(TopicName topic, int groupNo) {
        List<Replica> replicas = getByTopicAndGroup(topic, groupNo);
        if (replicas != null) {
            replicas.forEach(rp -> delete(rp.getId()));
        }
    }

    public void deleteByTopic(TopicName topic) {
        List<Replica> replicas = getByTopic(topic);
        if (replicas != null) {
            replicas.forEach(rp -> delete(rp.getId()));
        }
    }

    List<Replica> convert(List<IgnitePartitionGroupReplica> replicas) {
        List<Replica> result = new ArrayList<>();

        if (replicas != null) {
            replicas.forEach(e -> result.add(e));
        }
        return result;
    }
}
