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
package com.jd.joyqueue.service.impl;

import com.alibaba.fastjson.JSON;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.model.domain.Config;
import com.jd.joyqueue.model.query.QConfig;
import com.jd.joyqueue.service.ConfigService;
import com.jd.joyqueue.nsr.ConfigNameServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
@Service("configService")
public class ConfigServiceImpl  implements ConfigService {
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
    public List<Config> findByQuery(QConfig query) throws Exception {
        return configNameServerService.findByQuery(query);
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

//    @Override
//    public List<DataCenter> findAllDataCenter() {
//        List<Config> result = repository.findByQuery(new ListQuery<>(new QConfig(Config.GROUP_DATACENTER)));
//        if(null==result)return null;
//        List<DataCenter> dataCenters = new ArrayList<>();
//        result.forEach(config -> dataCenters.add((DataCenter) dataCenterConfigConverter.convert(config)));
//        return dataCenters;
//    }

    @Override
    public Config findByGroupAndKey(String group, String key) {
        try {
            QConfig qConfig = new QConfig();
            qConfig.setGroup(group);
            qConfig.setKey(key);
            List<Config> configList =  configNameServerService.findByQuery(qConfig);
            if (configList!= null && configList.size() > 0) {
                return configList.get(0);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("getListConfig exception",e);
        }
    }

    @Override
    public Config findById(String s) throws Exception {
        return configNameServerService.findById(s);
    }

    @Override
    public PageResult<Config> findByQuery(QPageQuery<QConfig> query) {
        try {
            return configNameServerService.findByQuery(query);
        } catch (Exception e) {
            throw new RuntimeException("findByQuery error",e);
        }
    }
}
