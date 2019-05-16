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
package com.jd.journalq.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.journalq.domain.Replica;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.convert.NsrReplicaConverter;
import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.domain.Topic;
import com.jd.journalq.model.query.QPartitionGroupReplica;
import com.jd.journalq.nsr.model.ReplicaQuery;
import com.jd.journalq.nsr.NameServerBase;
import com.jd.journalq.nsr.ReplicaServerService;
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
    public static final String LIST_REPLICA = "/replica/list";
    public static final String GETBYID_REPLICA = "/replica/getById";
    public static final String DELETEBYID_REPLICA = "/replica/deleteById";
    public static final String FINDBYQUERY_REPLICA = "/replica/findByQuery";
    private NsrReplicaConverter nsrReplicaConverter = new NsrReplicaConverter();

    @Override
    public PartitionGroupReplica findById(String id) throws Exception {
        String result = post(GETBYID_REPLICA,id);
        Replica replica = JSON.parseObject(result,Replica.class);
        return nsrReplicaConverter.revert(replica);
    }

    @Override
    public PageResult<PartitionGroupReplica> findByQuery(QPageQuery<QPartitionGroupReplica> query) throws Exception {
        ReplicaQuery replicaQuery =null;
        if (query != null && query.getQuery() != null) {
            QPartitionGroupReplica queryQuery = query.getQuery();
            replicaQuery = new ReplicaQuery(queryQuery.getTopic().getCode(),queryQuery.getNamespace().getCode(),queryQuery.getGroupNo());
        }
        QPageQuery<ReplicaQuery> replicaQueryQPageQuery = new QPageQuery<>(query.getPagination(),replicaQuery);

        String result = post(FINDBYQUERY_REPLICA,replicaQueryQPageQuery);

        PageResult<Replica> replicaPageResult = JSON.parseObject(result,new TypeReference<PageResult<Replica>>(){});
        if (replicaPageResult == null || replicaPageResult.getResult() == null) {
            return PageResult.empty();
        }
        return new PageResult<>(replicaPageResult.getPagination(), replicaPageResult.getResult().stream().map(replica -> nsrReplicaConverter.revert(replica)).collect(Collectors.toList()));
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

    @Override
    public List<PartitionGroupReplica> findByQuery(QPartitionGroupReplica query) throws Exception{
        try {
            ReplicaQuery replicaQuery =null;
            if (query != null) {
                replicaQuery = new ReplicaQuery();
                if (query.getTopic() != null) {
                    replicaQuery.setTopic(query.getTopic().getCode());
                }
                if (query.getNamespace() != null) {
                    replicaQuery.setNamespace(query.getNamespace().getCode());
                }
                replicaQuery.setGroup(query.getGroupNo());
            }
            String result = post(LIST_REPLICA,replicaQuery);
            List<Replica> replicas = JSON.parseArray(result,Replica.class);
            if (replicas == null || replicas.size() <=0) {
                return null;
            }
            return replicas.stream().map(replica -> nsrReplicaConverter.revert(replica)).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("LIST_REPLICA error",e);
            throw new RuntimeException("LIST_REPLICA error",e);
        }
    }

    @Override
    public int deleteByGroup(String topic, int groupNo) {
        try {
            List<PartitionGroupReplica>  list = findByQuery(new QPartitionGroupReplica(new Topic(topic),groupNo));
            for (PartitionGroupReplica partitionGroupReplica : list) {
                delete(partitionGroupReplica);
            }
        } catch (Exception e) {
            logger.error("deleteByGroup error",e);
        }
        return 1;
    }

    @Override
    public List<PartitionGroupReplica> findByTopic(String topic) {

        try {
           return findByQuery(new QPartitionGroupReplica(new Topic(topic)));
        } catch (Exception e) {
            logger.error("findByTopic error",e);
        }
        return null;
    }

    @Override
    public int deleteByTopic(String topic) {
        try {
            List<PartitionGroupReplica>  list = findByQuery(new QPartitionGroupReplica(new Topic(topic)));
            for (PartitionGroupReplica partitionGroupReplica : list) {
                delete(partitionGroupReplica);
            }
        } catch (Exception e) {
            logger.error("deleteByTopic error",e);
        }
        return 1;
    }
}
