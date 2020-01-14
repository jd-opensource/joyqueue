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
import org.joyqueue.nsr.ignite.dao.ConsumerDao;
import org.joyqueue.nsr.ignite.dao.IgniteDao;
import org.joyqueue.nsr.ignite.model.IgniteConsumer;
import org.joyqueue.nsr.model.ConsumerQuery;
import org.joyqueue.nsr.ignite.model.IgniteBaseModel;
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

public class IgniteConsumerDao implements ConsumerDao {
    public static final String CACHE_NAME = "consumer";
    public static CacheConfiguration<String, IgniteConsumer> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(IgniteBaseModel.SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgniteConsumer.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(IgniteConsumer.COLUMN_ID, String.class.getName());
        fields.put(IgniteConsumer.COLUMN_NAMESPACE, String.class.getName());
        fields.put(IgniteConsumer.COLUMN_TOPIC, String.class.getName());
        fields.put(IgniteConsumer.COLUMN_APP, String.class.getName());
        fields.put(IgniteConsumer.COLUMN_CLIENT_TYPE, Byte.class.getName());
        fields.put(IgniteConsumer.COLUMN_REFER, String.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(IgniteConsumer.COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    public IgniteConsumerDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgniteConsumer findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteConsumer model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteConsumer model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteConsumer> pageQuery(QPageQuery<ConsumerQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteConsumer> list(ConsumerQuery query) {
        return igniteDao.query(buildQuery(query));
    }


    private SqlQuery buildQuery(ConsumerQuery query) {
        IgniteDao.SimpleSqlBuilder sqlBuilder = IgniteDao.SimpleSqlBuilder.create(IgniteConsumer.class);
        if (query != null) {
            if (query.getTopic() != null && !query.getTopic().isEmpty()) {
                sqlBuilder.and(IgniteConsumer.COLUMN_TOPIC, query.getTopic());
            }
            if (query.getNamespace() != null && !query.getNamespace().isEmpty()) {
                sqlBuilder.and(IgniteConsumer.COLUMN_NAMESPACE, query.getNamespace());
            }
            if (query.getApp() != null && !query.getApp().isEmpty()) {
                sqlBuilder.and(IgniteConsumer.COLUMN_APP, query.getApp());
            }

            if (query.getReferer() != null && !query.getReferer().isEmpty()) {
                sqlBuilder.and(IgniteConsumer.COLUMN_REFER, query.getReferer());
            }

            if ((query.getClientType() != -1)) {
                sqlBuilder.and(IgniteConsumer.COLUMN_CLIENT_TYPE, query.getClientType());
            }
            if (query.getAppList() != null) {
                sqlBuilder.in(IgniteConsumer.COLUMN_REFER,query.getAppList());
            }
        }
        return sqlBuilder.build();
    }
}
