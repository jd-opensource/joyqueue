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
package io.chubao.joyqueue.handler.routing.command.application;


import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.ApplicationToken;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.query.QApplicationToken;
import io.chubao.joyqueue.service.ApplicationTokenService;

import java.util.Collections;
import java.util.List;

import static io.chubao.joyqueue.handler.Constants.APPLICATION;
import static io.chubao.joyqueue.handler.Constants.APP_ID;
import static io.chubao.joyqueue.handler.Constants.ID;

/**
 * Created by wangxiaofei1 on 2018/10/23.
 */
public class ApplicationTokenCommand extends NsrCommandSupport<ApplicationToken, ApplicationTokenService, QApplicationToken> {
    @Value(APPLICATION)
    protected Application application;

    @Path("search")
    public Response search(@PageQuery QPageQuery<QApplicationToken> qPageQuery) throws Exception {
        QApplicationToken query = qPageQuery.getQuery();
        List<ApplicationToken> appTokens = Collections.emptyList();

        if (query.getApplication() != null) {
            appTokens = service.findByApp(query.getApplication().getCode());
        }

        if (application != null) {
            appTokens = service.findByApp(application.getCode());
        }

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(appTokens.size());

        PageResult<ApplicationToken> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(appTokens);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("getByApp")
    public Response pageQuery(@PageQuery QPageQuery<QApplicationToken> qPageQuery) throws Exception {
        QApplicationToken query = qPageQuery.getQuery();
        query.setApplication(new Identity(application.getCode()));

        List<ApplicationToken> appTokenList = service.findByApp(application.getId());
        PageResult<ApplicationToken> result = new PageResult();
        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(appTokenList.size());
        pagination.setSize(appTokenList.size());
        result.setPagination(pagination);
        result.setResult(appTokenList);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("add")
    public Response add(@Body ApplicationToken model) throws Exception {
        int tokenCount = service.countByAppId(application.getId());
        if (tokenCount >= 5) {
            throw new ConfigException(ErrorCode.ExcessiveToken);
        }
        model.setApplication(application.identity());
        service.add(model);
        return Responses.success();
    }

    /**
     * 查询按应用id
     * @param appId
     * @return
     * @throws Exception
     */
    @Path("getByAppId")
    public Response findByAppId(@QueryParam(APP_ID) long appId) throws Exception {
        return Responses.success(service.findByApp(appId));
    }

    /**
     * 查询按应用id
     * @param id
     * @param model
     * @return
     * @throws Exception
     */
    @Override
    @Path("update")
    public Response update(@QueryParam(ID) String id,@Body ApplicationToken model) throws Exception {
        model.initializeTime();
        model.setApplication(application.identity());
        return Responses.success(service.update(model));
    }
    @Override
    @Path("delete")
    public Response delete(@QueryParam(ID) String id) throws Exception {
        ApplicationToken newModel = service.findById(Long.valueOf(id));
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }


}
