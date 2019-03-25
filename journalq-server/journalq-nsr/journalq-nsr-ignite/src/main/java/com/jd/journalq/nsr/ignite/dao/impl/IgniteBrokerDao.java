package com.jd.journalq.nsr.ignite.dao.impl;

import com.google.inject.Inject;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.nsr.ignite.dao.BrokerDao;
import com.jd.journalq.nsr.ignite.dao.IgniteDao;
import com.jd.journalq.nsr.ignite.model.IgniteBroker;
import com.jd.journalq.nsr.model.BrokerQuery;
import com.jd.journalq.nsr.ignite.model.IgniteBaseModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

public class IgniteBrokerDao implements BrokerDao {
    public static final String CACHE_NAME = "broker";
    public static CacheConfiguration<Integer, IgniteBroker> cacheCfg;
    private IgniteDao igniteDao;

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(IgniteBaseModel.SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(Integer.class.getName());
        queryEntity.setValueType(IgniteBroker.class.getName());
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(IgniteBroker.COLUMN_BROKER_ID, Integer.class.getName());
        fields.put(IgniteBroker.COLUMN_IP, String.class.getName());
        fields.put(IgniteBroker.COLUMN_PORT, String.class.getName());
        fields.put(IgniteBroker.COLUMN_DATA_CENTER, String.class.getName());
        fields.put(IgniteBroker.COLUMN_RETRY_TYPE, String.class.getName());
        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(IgniteBroker.COLUMN_BROKER_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    @Inject
    public IgniteBrokerDao(Ignite ignite) {
        this.igniteDao = new IgniteDao(ignite, cacheCfg);
    }

    @Override
    public IgniteBroker findById(Integer id) {
        return igniteDao.getById(id);
    }

    @Override
    public void add(IgniteBroker model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void addOrUpdate(IgniteBroker model) {
        igniteDao.addOrUpdate(model);
    }

    @Override
    public void deleteById(Integer id) {
        igniteDao.deleteById(id);
    }

    @Override
    public PageResult<IgniteBroker> pageQuery(QPageQuery<BrokerQuery> pageQuery) {
        return igniteDao.pageQuery(buildQuery(pageQuery.getQuery()), pageQuery.getPagination());
    }

    @Override
    public List<IgniteBroker> list(BrokerQuery query) {
        return igniteDao.query(buildQuery(query));
    }

    private SqlQuery buildQuery(BrokerQuery query) {
        IgniteDao.SimpleSqlBuilder sqlBuilder = IgniteDao.SimpleSqlBuilder.create(IgniteBroker.class);
        if (query != null) {
            if (query.getIp() != null && !query.getIp().isEmpty()) {
                sqlBuilder.and(IgniteBroker.COLUMN_IP, query.getIp());
            }

            if (query.getBrokerId() > 0) {
                sqlBuilder.and(IgniteBroker.COLUMN_BROKER_ID, query.getBrokerId());
            }
            if (query.getPort() > 0) {
                sqlBuilder.and(IgniteBroker.COLUMN_PORT, query.getPort());
            }
            if (query.getRetryType() != null && !query.getRetryType().isEmpty()) {
                sqlBuilder.and(IgniteBroker.COLUMN_RETRY_TYPE, query.getRetryType());
            }
            if (StringUtils.isNotEmpty(query.getKeyword())) {
                sqlBuilder.and(IgniteBroker.COLUMN_IP,query.getKeyword());
                if (NumberUtils.isNumber(query.getKeyword())){
                    sqlBuilder.or(IgniteBroker.COLUMN_BROKER_ID,Integer.valueOf(query.getKeyword()).intValue());
                }
            }
            if (query.getBrokerList() != null && !query.getBrokerList().isEmpty()) {
                sqlBuilder.in(IgniteBroker.COLUMN_BROKER_ID, query.getBrokerList());
            }
        }
        return sqlBuilder.build();
    }

    @Override
    public IgniteBroker getByIpAndPort(String ip, int port) {
        BrokerQuery brokerQuery = new BrokerQuery();
        brokerQuery.setIp(ip);
        brokerQuery.setPort(port);

        List<IgniteBroker> brokers = list(brokerQuery);
        if (brokers != null && brokers.size() > 1) {
            throw new IllegalStateException("broker not unique.");
        } else {
            return brokers == null || brokers.isEmpty() ? null : brokers.get(0);
        }
    }
}
