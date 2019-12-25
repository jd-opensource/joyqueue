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
import org.joyqueue.nsr.ignite.dao.ProducerConfigDao;
import org.joyqueue.nsr.ignite.model.IgniteProducerConfig;
import org.joyqueue.nsr.model.ProducerQuery;
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
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_LIMIT_TPS;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_LIMIT_TRAFFIC;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_ID;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_NAMESPACE;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_TOPIC;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_APP;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_NEAR_BY;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_ARCHIVE;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_SINGLE;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_WEIGHT;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_BLACK_LIST;
import static org.joyqueue.nsr.ignite.model.IgniteProducerConfig.COLUMN_TIMEOUT;

public class IgniteProducerConfigDao implements ProducerConfigDao {
    public static final String CACHE_NAME = "producer_config";
    public static CacheConfiguration<String, IgniteProducerConfig> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgniteProducerConfig.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_ID, String.class.getName());
        fields.put(COLUMN_NAMESPACE, String.class.getName());
        fields.put(COLUMN_TOPIC, String.class.getName());
        fields.put(COLUMN_APP, String.class.getName());
        fields.put(COLUMN_NEAR_BY, Boolean.class.getName());
        fields.put(COLUMN_ARCHIVE, Boolean.class.getName());
        fields.put(COLUMN_SINGLE, Boolean.class.getName());
        fields.put(COLUMN_WEIGHT, String.class.getName());
        fields.put(COLUMN_BLACK_LIST, String.class.getName());
        fields.put(COLUMN_TIMEOUT, Integer.class.getName());
        fields.put(COLUMN_LIMIT_TPS, Integer.class.getName());
        fields.put(COLUMN_LIMIT_TRAFFIC, Integer.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    public IgniteProducerConfigDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgniteProducerConfig findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteProducerConfig model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteProducerConfig model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteProducerConfig> pageQuery(QPageQuery<ProducerQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteProducerConfig> list(ProducerQuery query) {
        return igniteDao.query(buildQuery(query));
    }


    private SqlQuery buildQuery(ProducerQuery query) {
        IgniteDao.SimpleSqlBuilder sqlBuilder = IgniteDao.SimpleSqlBuilder.create(IgniteProducerConfig.class);
        if (query != null) {
            if (query.getTopic() != null && !query.getTopic().isEmpty()) {
                sqlBuilder.and(COLUMN_TOPIC, query.getTopic());
            }
            if (query.getNamespace() != null && !query.getNamespace().isEmpty()) {
                sqlBuilder.and(COLUMN_NAMESPACE, query.getNamespace());
            }
            if (query.getApp() != null && !query.getApp().isEmpty()) {
                sqlBuilder.and(COLUMN_APP, query.getApp());
            }
        }
        return sqlBuilder.build();
    }

}
