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

import com.google.inject.Inject;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.ignite.dao.IgniteDao;
import org.joyqueue.nsr.ignite.dao.TopicDao;
import org.joyqueue.nsr.ignite.model.IgniteTopic;
import org.joyqueue.nsr.model.TopicQuery;
import org.joyqueue.nsr.ignite.model.IgniteBaseModel;
import org.apache.commons.lang3.StringUtils;
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

public class IgniteTopicDao implements TopicDao {
    public static final String CACHE_NAME = "topic";
    public static CacheConfiguration<String, IgniteTopic> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(IgniteBaseModel.SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgniteTopic.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(IgniteTopic.COLUMN_ID, String.class.getName());
        fields.put(IgniteTopic.COLUMN_CODE, String.class.getName());
        fields.put(IgniteTopic.COLUMN_NAMESPACE, String.class.getName());
        fields.put(IgniteTopic.COLUMN_PARTITIONS, Short.class.getName());
        fields.put(IgniteTopic.COLUMN_PRIORITY_PARTITIONS, String.class.getName());
        fields.put(IgniteTopic.COLUMN_TYPE, Byte.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(IgniteTopic.COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }


    @Inject
    public IgniteTopicDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgniteTopic findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteTopic model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteTopic model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteTopic> pageQuery(QPageQuery<TopicQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteTopic> list(TopicQuery query) {
        return igniteDao.query(buildQuery(query));
    }


    private SqlQuery buildQuery(TopicQuery query) {
        IgniteDao.SimpleSqlBuilder sqlBuilder = IgniteDao.SimpleSqlBuilder.create(IgniteTopic.class);
        if (query != null) {
//            sqlBuilder.and("id !","__group_coordinators");
            if (query.getCode() != null && !query.getCode().isEmpty()) {
                sqlBuilder.and(IgniteTopic.COLUMN_CODE, query.getCode());
            }

            if (query.getNamespace() != null && !query.getNamespace().isEmpty()) {
                sqlBuilder.and(IgniteTopic.COLUMN_NAMESPACE, query.getNamespace());
            }
            if (query.getType() != null) {
                sqlBuilder.and(IgniteTopic.COLUMN_TYPE,query.getType());
            }
            if (StringUtils.isNotEmpty(query.getKeyword())) {
                sqlBuilder.like(IgniteTopic.COLUMN_CODE, query.getKeyword());
            }
        }
        return sqlBuilder.build();
    }
}
