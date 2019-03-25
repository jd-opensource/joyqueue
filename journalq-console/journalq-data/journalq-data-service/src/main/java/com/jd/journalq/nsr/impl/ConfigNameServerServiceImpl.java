package com.jd.journalq.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.convert.NsrConfigConverter;
import com.jd.journalq.model.domain.Config;
import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.query.QConfig;
import com.jd.journalq.nsr.model.ConfigQuery;
import com.jd.journalq.nsr.ConfigNameServerService;
import com.jd.journalq.nsr.NameServerBase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("configNameServerService")
public class ConfigNameServerServiceImpl extends NameServerBase implements ConfigNameServerService {

    public static final String ADD_CONFIG="/config/add";
    public static final String UPDATE_CONFIG="/config/update";
    public static final String REMOVE_CONFIG="/config/remove";
    public static final String GETBYID_CONFIG="/config/getById";
    public static final String LIST_CONFIG="/config/list";
    public static final String FINDBYQUERY_CONFIG="/config/findByQuery";

    private NsrConfigConverter nsrConfigConverter = new NsrConfigConverter();

    @Override
    public int add(Config config) throws Exception {
        com.jd.journalq.domain.Config nsrConfig = new com.jd.journalq.domain.Config();
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result = postWithLog(ADD_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.ADD.value(),nsrConfig.getId());
        return isSuccess(result);
    }

    @Override
    public int update(Config config) throws Exception {
        String result = post(GETBYID_CONFIG,config.getId());
        com.jd.journalq.domain.Config nsrConfig = JSON.parseObject(result, com.jd.journalq.domain.Config.class);
        if (nsrConfig == null) {
            nsrConfig = new com.jd.journalq.domain.Config();
        }
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result1 = postWithLog(UPDATE_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.UPDATE.value(),nsrConfig.getId());
        return isSuccess(result1);
    }

    @Override
    public int delete(Config config) throws Exception {
        com.jd.journalq.domain.Config nsrConfig = new com.jd.journalq.domain.Config();
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result = postWithLog(REMOVE_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.DELETE.value(),nsrConfig.getId());
        return isSuccess(result);
    }
    @Override
    public List<Config> findByQuery(QConfig qConfig) throws Exception {
        ConfigQuery configQuery = new ConfigQuery();
        if (qConfig != null) {
            configQuery.setGroup(qConfig.getGroup());
            configQuery.setKey(qConfig.getKey());
            configQuery.setKeyword(qConfig.getKeyword());
        }
        String result = post(LIST_CONFIG, configQuery);
        List<com.jd.journalq.domain.Config> configList = JSON.parseArray(result).toJavaList(com.jd.journalq.domain.Config.class);
        return configList.stream().map(config -> nsrConfigConverter.revert(config)).collect(Collectors.toList());
    }

    @Override
    public Config findById(String s) throws Exception {
        String result = post(GETBYID_CONFIG,s);
        com.jd.journalq.domain.Config nsrConfig = JSON.parseObject(result, com.jd.journalq.domain.Config.class);
        return nsrConfigConverter.revert(nsrConfig);
    }

    @Override
    public PageResult<Config> findByQuery(QPageQuery<QConfig> query) throws Exception {
        QPageQuery<ConfigQuery> pageQuery = new QPageQuery<>();
        pageQuery.setPagination(query.getPagination());
        ConfigQuery configQuery = new ConfigQuery();
        if (query.getQuery() != null ) {
            configQuery.setKey(query.getQuery().getKey());
            configQuery.setGroup(query.getQuery().getGroup());
            configQuery.setKeyword(query.getQuery().getKeyword());
        }
        pageQuery.setQuery(configQuery);
        String result = post(FINDBYQUERY_CONFIG,pageQuery);
        PageResult<com.jd.journalq.domain.Config> pageResult = JSON.parseObject(result,new TypeReference<PageResult<com.jd.journalq.domain.Config>>(){});
        PageResult<Config> configPageResult = new PageResult<>();
        configPageResult.setPagination(pageResult.getPagination());
        configPageResult.setResult(pageResult.getResult().stream().map(config -> nsrConfigConverter.revert(config)).collect(Collectors.toList()));
        return configPageResult;
    }
}
