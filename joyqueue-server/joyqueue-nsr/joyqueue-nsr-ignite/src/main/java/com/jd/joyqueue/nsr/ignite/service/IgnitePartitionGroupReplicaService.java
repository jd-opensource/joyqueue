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
package com.jd.joyqueue.nsr.ignite.service;

import com.google.inject.Inject;
import com.jd.joyqueue.domain.Replica;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.ignite.dao.PartitionGroupReplicaDao;
import com.jd.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica;
import com.jd.joyqueue.nsr.model.ReplicaQuery;
import com.jd.joyqueue.nsr.service.PartitionGroupReplicaService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:09 2018/8/13
 */
public class IgnitePartitionGroupReplicaService implements PartitionGroupReplicaService {

    private PartitionGroupReplicaDao replicaDao;

    @Inject
    public IgnitePartitionGroupReplicaService(PartitionGroupReplicaDao replicaDao) {
        this.replicaDao = replicaDao;
    }


    public IgnitePartitionGroupReplica toIgniteModel(Replica model) {
        return new IgnitePartitionGroupReplica(model);
    }

    @Override
    public void deleteByTopic(TopicName topic) {
        List<Replica> replicas = findByTopic(topic);
        if (replicas != null) {
            replicas.forEach(rp -> delete(rp));
        }
    }

    @Override
    public void deleteByTopicAndPartitionGroup(TopicName topic, int groupNo) {
        List<Replica> replicas = findByTopicAndGrPartitionGroup(topic, groupNo);
        if (replicas != null) {
            replicas.forEach(rp -> delete(rp));
        }
    }

    @Override
    public List<Replica> findByTopic(TopicName topic) {
        return this.list(new ReplicaQuery(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Replica> findByTopicAndGrPartitionGroup(TopicName topic, int groupNo) {
        return this.list(new ReplicaQuery(topic.getCode(), topic.getNamespace(), groupNo));
    }

    @Override
    public List<Replica> findByBrokerId(Integer brokerId) {
        return this.list(new ReplicaQuery(brokerId));
    }

    @Override
    public Replica getById(String id) {
        return replicaDao.findById(id);
    }

    @Override
    public Replica get(Replica model) {
        return replicaDao.findById(toIgniteModel(model).getId());
    }

    @Override
    public void addOrUpdate(Replica replica) {
        replicaDao.addOrUpdate(toIgniteModel(replica));
    }

    @Override
    public void deleteById(String id) {
        replicaDao.deleteById(id);
    }

    @Override
    public void delete(Replica model) {
        replicaDao.deleteById(toIgniteModel(model).getId());
    }

    @Override
    public List<Replica> list() {
        return convert(replicaDao.list(null));
    }

    @Override
    public List<Replica> list(ReplicaQuery query) {
        return convert(replicaDao.list(query));
    }

    @Override
    public PageResult<Replica> pageQuery(QPageQuery<ReplicaQuery> pageQuery) {
        PageResult<IgnitePartitionGroupReplica> pageResult = replicaDao.pageQuery(pageQuery);
        return new PageResult<>(pageResult.getPagination(), convert(pageResult.getResult()));
    }


    List<Replica> convert(List<IgnitePartitionGroupReplica> replicas) {
        List<Replica> result = new ArrayList<>();

        if (replicas != null) {
            replicas.forEach(e -> result.add(e));
        }
        return result;
    }
}
