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
import org.joyqueue.nsr.ignite.dao.ConfigDao;
import org.joyqueue.nsr.ignite.dao.IgniteDao;
import org.joyqueue.nsr.ignite.model.IgniteConfig;
import org.joyqueue.nsr.model.ConfigQuery;
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

import static org.joyqueue.nsr.ignite.model.IgniteConfig.COLUMN_ID;
import static org.joyqueue.nsr.ignite.model.IgniteConfig.COLUMN_CFG_KEY;
import static org.joyqueue.nsr.ignite.model.IgniteConfig.COLUMN_CFG_VALUE;
import static org.joyqueue.nsr.ignite.model.IgniteConfig.COLUMN_CFG_GROUP;

public class IgniteConfigDao implements ConfigDao {
    public static final String CACHE_NAME = "config";
    public static CacheConfiguration<String, IgniteConfig> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(IgniteBaseModel.SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgniteConfig.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_ID, String.class.getName());
        fields.put(COLUMN_CFG_GROUP, String.class.getName());
        fields.put(COLUMN_CFG_KEY, String.class.getName());
        fields.put(COLUMN_CFG_VALUE, String.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    public IgniteConfigDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgniteConfig findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteConfig model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteConfig model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteConfig> pageQuery(QPageQuery<ConfigQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteConfig> list(ConfigQuery query) {
        return igniteDao.query(buildQuery(query));
    }

    private SqlQuery buildQuery(ConfigQuery query) {
        IgniteDao.SimpleSqlBuilder sqlBuilder = IgniteDao.SimpleSqlBuilder.create(IgniteConfig.class);
        if (query != null) {
            if (query.getKey() != null && !query.getKey().isEmpty()) {
                sqlBuilder.and(COLUMN_CFG_KEY, query.getKey());
            }

            if (query.getGroup() != null && !query.getGroup().isEmpty()) {
                sqlBuilder.and(COLUMN_CFG_GROUP, query.getGroup());
            }
            if (StringUtils.isNotEmpty(query.getKeyword())) {
                sqlBuilder.and(COLUMN_CFG_KEY,query.getKeyword()).or(COLUMN_CFG_GROUP,query.getKeyword());
            }

        }
        return sqlBuilder.build();
    }
}
