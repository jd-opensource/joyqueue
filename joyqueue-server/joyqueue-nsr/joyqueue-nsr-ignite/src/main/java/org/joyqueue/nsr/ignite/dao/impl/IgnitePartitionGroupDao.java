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
import org.joyqueue.nsr.ignite.dao.PartitionGroupDao;
import org.joyqueue.nsr.ignite.model.IgnitePartitionGroup;
import org.joyqueue.nsr.model.PartitionGroupQuery;
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
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_ID;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_NAMESPACE;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_TOPIC;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_GROUP;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_LEADER;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_ISR;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_TERM;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_PARTITIONS;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_LEARNERS;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_REPLICAS;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_OUT_SYNC_REPLICAS;
import static org.joyqueue.nsr.ignite.model.IgnitePartitionGroup.COLUMN_ELECT_TYPE;

public class IgnitePartitionGroupDao implements PartitionGroupDao {
    public static final String CACHE_NAME = "partition_group";
    public static CacheConfiguration<String, IgnitePartitionGroup> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgnitePartitionGroup.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_ID, String.class.getName());
        fields.put(COLUMN_NAMESPACE, String.class.getName());
        fields.put(COLUMN_TOPIC, String.class.getName());
        fields.put(COLUMN_GROUP, Integer.class.getName());
        fields.put(COLUMN_LEADER, Integer.class.getName());
        fields.put(COLUMN_ISR, String.class.getName());
        fields.put(COLUMN_TERM, Integer.class.getName());
        fields.put(COLUMN_PARTITIONS, String.class.getName());
        fields.put(COLUMN_LEARNERS, String.class.getName());
        fields.put(COLUMN_REPLICAS, String.class.getName());
        fields.put(COLUMN_OUT_SYNC_REPLICAS, String.class.getName());
        fields.put(COLUMN_ELECT_TYPE, String.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    public IgnitePartitionGroupDao(Ignite ignite) {
        igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgnitePartitionGroup findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgnitePartitionGroup model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgnitePartitionGroup model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgnitePartitionGroup> pageQuery(QPageQuery<PartitionGroupQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgnitePartitionGroup> list(PartitionGroupQuery query) {
        return igniteDao.query(buildQuery(query));
    }

    private SqlQuery buildQuery(PartitionGroupQuery query) {

        IgniteDao.SimpleSqlBuilder builder = IgniteDao.SimpleSqlBuilder.create(IgnitePartitionGroup.class);
        if (query != null) {
            if (query.getGroup() > 0) {
                builder.and("`"+COLUMN_GROUP+"`", query.getGroup());
            }

            if (query.getTopic() != null && !query.getTopic().isEmpty()) {
                builder.and(COLUMN_TOPIC, query.getTopic());
            }
            if (query.getNamespace() != null) {
                builder.and(COLUMN_NAMESPACE, query.getNamespace());
            }
        }

        return builder.build();
    }

}
