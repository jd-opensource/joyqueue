/**
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
package com.jd.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.convert.NsrConfigConverter;
import com.jd.joyqueue.model.domain.Config;
import com.jd.joyqueue.model.domain.OperLog;
import com.jd.joyqueue.model.query.QConfig;
import com.jd.joyqueue.nsr.model.ConfigQuery;
import com.jd.joyqueue.nsr.ConfigNameServerService;
import com.jd.joyqueue.nsr.NameServerBase;
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
        com.jd.joyqueue.domain.Config nsrConfig = new com.jd.joyqueue.domain.Config();
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result = postWithLog(ADD_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.ADD.value(),nsrConfig.getId());
        return isSuccess(result);
    }

    @Override
    public int update(Config config) throws Exception {
        String result = post(GETBYID_CONFIG,config.getId());
        com.jd.joyqueue.domain.Config nsrConfig = JSON.parseObject(result, com.jd.joyqueue.domain.Config.class);
        if (nsrConfig == null) {
            nsrConfig = new com.jd.joyqueue.domain.Config();
        }
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result1 = postWithLog(UPDATE_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.UPDATE.value(),nsrConfig.getId());
        return isSuccess(result1);
    }

    @Override
    public int delete(Config config) throws Exception {
        com.jd.joyqueue.domain.Config nsrConfig = new com.jd.joyqueue.domain.Config();
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
        List<com.jd.joyqueue.domain.Config> configList = JSON.parseArray(result).toJavaList(com.jd.joyqueue.domain.Config.class);
        return configList.stream().map(config -> nsrConfigConverter.revert(config)).collect(Collectors.toList());
    }

    @Override
    public Config findById(String s) throws Exception {
        String result = post(GETBYID_CONFIG,s);
        com.jd.joyqueue.domain.Config nsrConfig = JSON.parseObject(result, com.jd.joyqueue.domain.Config.class);
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
        PageResult<com.jd.joyqueue.domain.Config> pageResult = JSON.parseObject(result,new TypeReference<PageResult<com.jd.joyqueue.domain.Config>>(){});
        PageResult<Config> configPageResult = new PageResult<>();
        configPageResult.setPagination(pageResult.getPagination());
        configPageResult.setResult(pageResult.getResult().stream().map(config -> nsrConfigConverter.revert(config)).collect(Collectors.toList()));
        return configPageResult;
    }
}
