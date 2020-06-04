/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.handler.routing.command.config;

import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Config;
import org.joyqueue.model.query.QConfig;
import org.joyqueue.service.ConfigService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public class ConfigCommand extends NsrCommandSupport<Config, ConfigService, QConfig> {

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QConfig> qPageQuery) throws Exception {
        List<Config> allConfigs = service.getAll();
        List<Config> configs = new ArrayList<>();
        QConfig qConfig = qPageQuery.getQuery();

        if (StringUtils.isNotBlank(qConfig.getKey()) ||
                StringUtils.isNotBlank(qConfig.getGroup()) ||
                StringUtils.isNotBlank(qConfig.getKeyword())) {
            for (Config config : allConfigs) {
                if (StringUtils.isNotBlank(qConfig.getKey()) && StringUtils.containsIgnoreCase(config.getKey(), qConfig.getKey())) {
                    configs.add(config);
                } else if (StringUtils.isNotBlank(qConfig.getGroup()) && StringUtils.containsIgnoreCase(config.getGroup(), qConfig.getGroup())) {
                    configs.add(config);
                } else if (StringUtils.isNotBlank(qConfig.getKeyword())) {
                    if (StringUtils.containsIgnoreCase(config.getKey(), qConfig.getKeyword()) ||
                        StringUtils.containsIgnoreCase(config.getGroup(), qConfig.getKeyword())) {
                        configs.add(config);
                    }
                }
            }
        } else {
            configs.addAll(allConfigs);
        }

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(configs.size());

        PageResult<Config> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(configs);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Override
    @Path("delete")
    public Response delete(@QueryParam(Constants.ID) String id) throws Exception {
        Config newModel = service.findById(id);
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }

}
