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
import org.joyqueue.nsr.ignite.dao.ConsumerConfigDao;
import org.joyqueue.nsr.ignite.dao.IgniteDao;
import org.joyqueue.nsr.ignite.model.IgniteConsumerConfig;
import org.joyqueue.nsr.model.ConsumerQuery;
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
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_NAMESPACE;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_TOPIC;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_APP;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_NEAR_BY;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_ARCHIVE;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_RETRY;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_CONCURRENT;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_ACK_TIMEOUT;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_BATCH_SIZE;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_BLACK_LIST;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_DELAY;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_MAX_RETRYS;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_MAX_RETRY_DELAY;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_RETRY_DELAY;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_EXPIRE_TIME;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_PAUSED;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_ERROR_TIMES;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_MAX_PARTITION_NUM;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_RETRY_READ_PROBABILITY;
import static org.joyqueue.nsr.ignite.model.IgniteConsumerConfig.COLUMN_ID;

public class IgniteConsumerConfigDao implements ConsumerConfigDao {
    public static final String cacheName = "consumer_config";
    public static CacheConfiguration<String, IgniteConsumerConfig> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(cacheName);
        cacheCfg.setSqlSchema(SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgniteConsumerConfig.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_ID, String.class.getName());
        fields.put(COLUMN_NAMESPACE, String.class.getName());
        fields.put(COLUMN_TOPIC, String.class.getName());
        fields.put(COLUMN_APP, String.class.getName());
        fields.put(COLUMN_NEAR_BY, Boolean.class.getName());
        fields.put(COLUMN_ARCHIVE, Boolean.class.getName());
        fields.put(COLUMN_RETRY, Boolean.class.getName());
        fields.put(COLUMN_CONCURRENT, Integer.class.getName());
        fields.put(COLUMN_ACK_TIMEOUT, Integer.class.getName());
        fields.put(COLUMN_BATCH_SIZE, Short.class.getName());
        fields.put(COLUMN_BLACK_LIST, String.class.getName());
        fields.put(COLUMN_DELAY, Integer.class.getName());
        fields.put(COLUMN_MAX_RETRYS, Integer.class.getName());
        fields.put(COLUMN_MAX_RETRY_DELAY, Integer.class.getName());
        fields.put(COLUMN_RETRY_DELAY, Integer.class.getName());
        fields.put(COLUMN_EXPIRE_TIME, Integer.class.getName());
        fields.put(COLUMN_PAUSED, Boolean.class.getName());
        fields.put(COLUMN_ERROR_TIMES, Integer.class.getName());
        fields.put(COLUMN_MAX_PARTITION_NUM, Integer.class.getName());
        fields.put(COLUMN_RETRY_READ_PROBABILITY, Integer.class.getName());
        fields.put(COLUMN_LIMIT_TPS, Integer.class.getName());
        fields.put(COLUMN_LIMIT_TRAFFIC, Integer.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(cacheName);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    public IgniteConsumerConfigDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }


    @Override
    public IgniteConsumerConfig findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteConsumerConfig model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteConsumerConfig model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteConsumerConfig> pageQuery(QPageQuery<ConsumerQuery> pageQuery) {
        return igniteDao.pageQuery(buildSqlQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteConsumerConfig> list(ConsumerQuery query) {
        return igniteDao.query(buildSqlQuery(query));
    }

    private SqlQuery buildSqlQuery(ConsumerQuery query) {
        IgniteDao.SimpleSqlBuilder sqlBuilder = IgniteDao.SimpleSqlBuilder.create(IgniteConsumerConfig.class);
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
