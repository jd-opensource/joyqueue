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
import org.joyqueue.nsr.ignite.dao.DataCenterDao;
import org.joyqueue.nsr.ignite.dao.IgniteDao;
import org.joyqueue.nsr.ignite.model.IgniteDataCenter;
import org.joyqueue.nsr.model.DataCenterQuery;
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

public class IgniteDataCenterDao implements DataCenterDao {
    public static final String CACHE_NAME = "datacenter";
    public static CacheConfiguration<String, IgniteDataCenter> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(IgniteBaseModel.SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(String.class.getName());
        queryEntity.setValueType(IgniteDataCenter.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(IgniteDataCenter.COLUMN_ID, String.class.getName());
        fields.put(IgniteDataCenter.COLUMN_REGION, String.class.getName());
        fields.put(IgniteDataCenter.COLUMN_CODE, String.class.getName());
        fields.put(IgniteDataCenter.COLUMN_NAME, String.class.getName());
        fields.put(IgniteDataCenter.COLUMN_URL, String.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(IgniteDataCenter.COLUMN_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    @Inject
    public IgniteDataCenterDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }


    private SqlQuery buildQuery(DataCenterQuery query) {
        IgniteDao.SimpleSqlBuilder sqlBuilder = IgniteDao.SimpleSqlBuilder.create(IgniteDataCenter.class);
        if (query != null) {
            if (query.getCode() != null && !query.getCode().isEmpty()) {
                sqlBuilder.and(IgniteDataCenter.COLUMN_CODE, query.getCode());
            }

            if (query.getRegion() != null && !query.getRegion().isEmpty()) {
                sqlBuilder.and(IgniteDataCenter.COLUMN_REGION, query.getRegion());
            }

        }
        return sqlBuilder.build();
    }


    @Override
    public IgniteDataCenter findById(String id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteDataCenter model) {
        this.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteDataCenter model) {
        igniteDao.addOrUpdate(new IgniteDataCenter(model));
    }

    @Override
    public void deleteById(String id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteDataCenter> pageQuery(QPageQuery<DataCenterQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteDataCenter> list(DataCenterQuery query) {
        return igniteDao.query(buildQuery(query));
    }
}
