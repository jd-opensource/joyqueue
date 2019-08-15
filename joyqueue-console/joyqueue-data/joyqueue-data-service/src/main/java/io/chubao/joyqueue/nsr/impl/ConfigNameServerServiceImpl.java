/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.convert.NsrConfigConverter;
import io.chubao.joyqueue.model.domain.Config;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QConfig;
import io.chubao.joyqueue.nsr.model.ConfigQuery;
import io.chubao.joyqueue.nsr.ConfigNameServerService;
import io.chubao.joyqueue.nsr.NameServerBase;
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
        io.chubao.joyqueue.domain.Config nsrConfig = new io.chubao.joyqueue.domain.Config();
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result = postWithLog(ADD_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.ADD.value(),nsrConfig.getId());
        return isSuccess(result);
    }

    @Override
    public int update(Config config) throws Exception {
        String result = post(GETBYID_CONFIG,config.getId());
        io.chubao.joyqueue.domain.Config nsrConfig = JSON.parseObject(result, io.chubao.joyqueue.domain.Config.class);
        if (nsrConfig == null) {
            nsrConfig = new io.chubao.joyqueue.domain.Config();
        }
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result1 = postWithLog(UPDATE_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.UPDATE.value(),nsrConfig.getId());
        return isSuccess(result1);
    }

    @Override
    public int delete(Config config) throws Exception {
        io.chubao.joyqueue.domain.Config nsrConfig = new io.chubao.joyqueue.domain.Config();
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
        List<io.chubao.joyqueue.domain.Config> configList = JSON.parseArray(result).toJavaList(io.chubao.joyqueue.domain.Config.class);
        return configList.stream().map(config -> nsrConfigConverter.revert(config)).collect(Collectors.toList());
    }

    @Override
    public Config findById(String s) throws Exception {
        String result = post(GETBYID_CONFIG,s);
        io.chubao.joyqueue.domain.Config nsrConfig = JSON.parseObject(result, io.chubao.joyqueue.domain.Config.class);
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
        PageResult<io.chubao.joyqueue.domain.Config> pageResult = JSON.parseObject(result,new TypeReference<PageResult<io.chubao.joyqueue.domain.Config>>(){});
        PageResult<Config> configPageResult = new PageResult<>();
        configPageResult.setPagination(pageResult.getPagination());
        configPageResult.setResult(pageResult.getResult().stream().map(config -> nsrConfigConverter.revert(config)).collect(Collectors.toList()));
        return configPageResult;
    }
}
