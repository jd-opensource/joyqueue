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
package io.chubao.joyqueue.handler.routing.command.config;

import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Config;
import io.chubao.joyqueue.model.query.QConfig;
import io.chubao.joyqueue.service.ConfigService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.chubao.joyqueue.handler.Constants.ID;


/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public class ConfigCommand extends NsrCommandSupport<Config,ConfigService,QConfig> {

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QConfig> qPageQuery) throws Exception {
        List<Config> allConfigs = service.getAll();
        List<Config> configs = new ArrayList<>();
        QConfig qConfig = qPageQuery.getQuery();

        for (Config config : allConfigs) {
            if (StringUtils.isNotBlank(qConfig.getKey()) && StringUtils.isNotBlank(qConfig.getGroup())) {
                if (config.getKey().equals(qConfig.getKey()) || config.getGroup().equals(qConfig.getGroup())) {
                    configs.add(config);
                }
            } else if (StringUtils.isNotBlank(qConfig.getKey())) {
                if (config.getKey().equals(qConfig.getKey())) {
                    configs.add(config);
                }
            } else {
                configs.add(config);
            }
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
    public Response delete(@QueryParam(ID) String id) throws Exception {
        Config newModel = service.findById(id);
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }

}
