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
package org.joyqueue.handler.routing.command.application;


import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.ApplicationToken;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.query.QApplicationToken;
import org.joyqueue.service.ApplicationTokenService;

import java.util.Collections;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/10/23.
 */
public class ApplicationTokenCommand extends NsrCommandSupport<ApplicationToken, ApplicationTokenService, QApplicationToken> {
    @Value(Constants.APPLICATION)
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

        // 权限约束：普通用户只有该应用下用户才能添加用户
        super.validatePrivilege(application.getCode());

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
        // 权限约束：普通用户只有该应用下用户才能添加用户
        super.validatePrivilege(application.getCode());

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
    public Response findByAppId(@QueryParam(Constants.APP_ID) long appId) throws Exception {
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
    public Response update(@QueryParam(Constants.ID) String id, @Body ApplicationToken model) throws Exception {
        // 权限约束：普通用户只有该应用下用户才能添加用户
        super.validatePrivilege(application.getCode());
        model.initializeTime();
        model.setApplication(application.identity());
        return Responses.success(service.update(model));
    }
    @Override
    @Path("delete")
    public Response delete(@QueryParam(Constants.ID) String id) throws Exception {
        // 权限约束：普通用户只有该应用下用户才能添加用户
        super.validatePrivilege(application.getCode());
        ApplicationToken newModel = service.findById(Long.valueOf(id));
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }


}
