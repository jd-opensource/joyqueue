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
package org.joyqueue.service.impl;

import com.alibaba.fastjson.JSON;
import org.joyqueue.model.domain.Config;
import org.joyqueue.nsr.ConfigNameServerService;
import org.joyqueue.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
@Service("configService")
public class ConfigServiceImpl implements ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

    @Resource
    private ConfigNameServerService configNameServerService;
    @Override
    public int add(Config model) {
        try {
            configNameServerService.add(model);
        } catch (Exception e) {
            String errorMsg = String.format("update naming service failed,%s", JSON.toJSONString(model));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Override
    public int update(Config model) {
        try {
            configNameServerService.update(model);
        } catch (Exception e) {
            String errorMsg = String.format("update naming service failed,%s", JSON.toJSONString(model));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Override
    public int delete(Config model) {
        try {
            configNameServerService.delete(model);
        } catch (Exception e) {
            String errorMsg = String.format("update naming service failed,%s", JSON.toJSONString(model));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Override
    public Config findByGroupAndKey(String group, String key) {
        try {
            return configNameServerService.findByGroupAndKey(group, key);
        } catch (Exception e) {
            logger.error("", e);
            throw new RuntimeException("getListConfig exception",e);
        }
    }

    @Override
    public List<Config> getAll() throws Exception {
        return configNameServerService.getAll();
    }

    @Override
    public Config findById(String s) throws Exception {
        return configNameServerService.findById(s);
    }
}
