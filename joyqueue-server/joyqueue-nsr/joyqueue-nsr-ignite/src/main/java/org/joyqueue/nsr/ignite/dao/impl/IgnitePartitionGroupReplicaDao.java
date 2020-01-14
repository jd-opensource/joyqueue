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
package org.joyqueue.nsr.ignite.dao.impl;

import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.ignite.dao.IgniteDao;
import org.joyqueue.nsr.ignite.dao.PartitionGroupReplicaDao;
import org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica;
import org.joyqueue.nsr.model.ReplicaQuery;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.joyqueue.nsr.ignite.model.IgniteBaseModel.SCHEMA;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica.COLUMN_TOPIC;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica.COLUMN_NAMESPACE;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica.COLUMN_BROKER_ID;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica.COLUMN_GROUP_NO;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroupReplica.COLUMN_ID;

public class IgnitePartitionGroupReplicaDao implements PartitionGroupReplicaDao {
    public static CacheConfiguration<String, IgnitePartitionGroupReplica> cacheCfg;
    public static final String CACHE_NAME = "partition_group_replica";
    private IgniteDao igniteDao;


    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgnitePartitionGroupReplica.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_ID, String.class.getName());
        fields.put(COLUMN_TOPIC, String.class.getName());
        fields.put(COLUMN_NAMESPACE, String.class.getName());
        fields.put(COLUMN_BROKER_ID, Integer.class.getName());
        fields.put(COLUMN_GROUP_NO, Integer.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }


    public IgnitePartitionGroupReplicaDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgnitePartitionGroupReplica findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgnitePartitionGroupReplica model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgnitePartitionGroupReplica model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgnitePartitionGroupReplica> pageQuery(QPageQuery<ReplicaQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgnitePartitionGroupReplica> list(ReplicaQuery query) {
        return igniteDao.query(buildQuery(query));
    }

    private SqlQuery buildQuery(ReplicaQuery query) {

        IgniteDao.SimpleSqlBuilder builder = IgniteDao.SimpleSqlBuilder.create(IgnitePartitionGroupReplica.class);
        if (query != null) {
            if (query.getGroup() > -1) {
                builder.and(COLUMN_GROUP_NO, query.getGroup());
            }

            if (query.getTopic() != null && !query.getTopic().isEmpty()) {
                builder.and(COLUMN_TOPIC, query.getTopic());
            }
            if (query.getNamespace() != null) {
                builder.and(COLUMN_NAMESPACE, query.getNamespace());
            }

            if (query.getBrokerId() > 0) {
                builder.and(COLUMN_BROKER_ID, query.getBrokerId());
            }
        }

        return builder.build();
    }

}
