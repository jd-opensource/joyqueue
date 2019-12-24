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
import org.joyqueue.nsr.ignite.dao.NamespaceDao;
import org.joyqueue.nsr.ignite.model.IgniteNamespace;
import org.joyqueue.nsr.model.NamespaceQuery;
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

import static org.joyqueue.nsr.ignite.model.IgniteNamespace.COLUMN_CODE;
import static org.joyqueue.nsr.ignite.model.IgniteNamespace.COLUMN_NAME;

public class IgniteNamespaceDao implements NamespaceDao {
    public static final String CACHE_NAME = "namespace";
    public static CacheConfiguration<Integer, IgniteNamespace> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(IgniteBaseModel.SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgniteNamespace.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_CODE, String.class.getName());
        fields.put(COLUMN_NAME, String.class.getName());

        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_CODE)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    public IgniteNamespaceDao(Ignite ignite) {
        igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgniteNamespace findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteNamespace model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteNamespace model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteNamespace> pageQuery(QPageQuery<NamespaceQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteNamespace> list(NamespaceQuery query) {
        return igniteDao.query(buildQuery(query));
    }

    SqlQuery buildQuery(NamespaceQuery query) {
        IgniteDao.SimpleSqlBuilder builder = IgniteDao.SimpleSqlBuilder.create(IgniteNamespace.class);
        if (query != null && (query.getCode() != null && !query.getCode().isEmpty())) {
            builder.and(COLUMN_CODE, query.getCode());
        }

        return builder.build();
    }
}
