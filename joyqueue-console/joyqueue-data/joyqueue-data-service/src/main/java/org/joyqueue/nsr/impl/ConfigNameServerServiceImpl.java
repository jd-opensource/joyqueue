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
package org.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import org.joyqueue.convert.NsrConfigConverter;
import org.joyqueue.model.domain.Config;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.nsr.ConfigNameServerService;
import org.joyqueue.nsr.NameServerBase;
import org.joyqueue.nsr.model.ConfigQuery;
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
    public static final String GETBYGROUPANDKEY="/config/getByGroupAndKey";
    public static final String LIST_CONFIG="/config/list";

    private NsrConfigConverter nsrConfigConverter = new NsrConfigConverter();

    @Override
    public int add(Config config) throws Exception {
        org.joyqueue.domain.Config nsrConfig = new org.joyqueue.domain.Config();
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result = postWithLog(ADD_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.ADD.value(),nsrConfig.getId());
        return isSuccess(result);
    }

    @Override
    public int update(Config config) throws Exception {
        String result = post(GETBYID_CONFIG,config.getId());
        org.joyqueue.domain.Config nsrConfig = JSON.parseObject(result, org.joyqueue.domain.Config.class);
        if (nsrConfig == null) {
            nsrConfig = new org.joyqueue.domain.Config();
        }
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result1 = postWithLog(UPDATE_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.UPDATE.value(),nsrConfig.getId());
        return isSuccess(result1);
    }

    @Override
    public int delete(Config config) throws Exception {
        org.joyqueue.domain.Config nsrConfig = new org.joyqueue.domain.Config();
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        nsrConfig.setGroup(config.getGroup());
        String result = postWithLog(REMOVE_CONFIG, nsrConfig,OperLog.Type.CONFIG.value(),OperLog.OperType.DELETE.value(),nsrConfig.getId());
        return isSuccess(result);
    }

    @Override
    public Config findById(String s) throws Exception {
        String result = post(GETBYID_CONFIG,s);
        org.joyqueue.domain.Config nsrConfig = JSON.parseObject(result, org.joyqueue.domain.Config.class);
        return nsrConfigConverter.revert(nsrConfig);
    }

    @Override
    public Config findByGroupAndKey(String group, String key) throws Exception {
        ConfigQuery configQuery = new ConfigQuery();
        configQuery.setGroup(group);
        configQuery.setKey(key);

        String result = post(GETBYGROUPANDKEY, configQuery);
        org.joyqueue.domain.Config nsrConfig = JSON.parseObject(result, org.joyqueue.domain.Config.class);
        return nsrConfigConverter.revert(nsrConfig);
    }

    @Override
    public List<Config> getAll() throws Exception {
        String result = post(LIST_CONFIG, null);
        List<org.joyqueue.domain.Config> configList = JSON.parseArray(result).toJavaList(org.joyqueue.domain.Config.class);
        return configList.stream().map(config -> nsrConfigConverter.revert(config)).collect(Collectors.toList());
    }
}
