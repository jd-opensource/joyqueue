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
import org.joyqueue.handler.Constants;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.routing.command.CommandSupport;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.*;
import org.joyqueue.model.query.QUser;
import org.joyqueue.service.ApplicationService;
import org.joyqueue.service.ApplicationUserService;
import org.joyqueue.service.UserService;

import java.util.Date;


/**
 * Created by wangxiaofei1 on 2018/10/23.
 */
public class ApplicationUserCommand extends CommandSupport<ApplicationUser, UserService, QUser> {
    @QueryParam(Constants.APP_ID)
    protected Long appId;//应用id
    @Value(nullable = false)
    protected UserService userService;
    @Value(nullable = false)
    protected ApplicationUserService applicationUserService;
    @Value(nullable = false)
    protected ApplicationService applicationService;
    @Value(Constants.APPLICATION)
    protected Application application;
    @Value(Constants.USER_KEY)
    protected User session;

    @Path("add")
    public Response add(@Body ApplicationUser applicationUser) throws Exception {
        // 参数检查
        if (applicationUser.getUser() == null) {
            throw new ConfigException(ErrorCode.BadRequest, "没有传入User参数!");
        }
        if(null == application) {
            throw new ConfigException(ErrorCode.BadRequest, "找不到此应用!");
        }
        // 权限约束：普通用户只有该应用下用户才能添加用户
        super.validatePrivilege(application.getCode());
        // 查找/同步用户
        applicationUser.setApplication(application.identity());
        applicationUser.setCreateBy(operator);
        applicationUser.setUpdateBy(operator);
        Identity userIdentity = applicationUser.getUser();
        User user = userIdentity.getId() != null ? userService.findById(userIdentity.getId()) : userService.findByCode(userIdentity.getCode());
        if (user == null) {
            return Responses.error(404,"填写账号在此系统找不到");
//            UserInfo info = syncService.syncUser(user);
//            if (info == null) {
//                throw new ConfigException(ErrorCode.UserNotExists);
//            }
//            info.setUser(operator);
//            user = syncService.addOrUpdateUser(info);
        }
        applicationUser.setUser(user.identity());
        // 保存appUser
        int count = applicationUserService.add(applicationUser);
        if (count <= 0) {
            throw new ConfigException(addErrorCode());
        }
        return Responses.success(applicationUser);
    }

    @Override
    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QUser> qPageQuery) throws Exception {
        if (qPageQuery.getQuery()== null) {
            QUser qUser = new QUser();
            qUser.setAppId(appId);
            qPageQuery.setQuery(new QUser());
        } else {
            qPageQuery.getQuery().setAppId(appId);
        }
        return super.pageQuery(qPageQuery);
    }

    @Path("getByAppCode")
    public Response getByAppCode(@QueryParam(Constants.APP_CODE) String appCode) throws Exception {
        Application app = applicationService.findByCode(appCode);
        if (app == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists, String.format("can not app find by code %s", appCode));
        }
        QPageQuery<QUser> qPageQuery = new QPageQuery<>();
        QUser qUser = new QUser();
        qUser.setAppId(app.getId());
        qPageQuery.setQuery(qUser);
        qPageQuery.setPagination(Pagination.newPagination(0, Pagination.MAX_SIZE));
        return super.pageQuery(qPageQuery);
    }

    @Path("delete")
    public Response delete(@QueryParam(Constants.APP_ID) Long appId, @QueryParam(Constants.USER_ID) Long userId) throws Exception {
        // 权限约束：普通用户只有该应用下用户才能添加用户
        super.validatePrivilege(application.getCode());
        ApplicationUser appUser = service.findAppUserByAppIdAndUserId(appId, userId);
        appUser.setStatus(BaseModel.DELETED);
        appUser.setUpdateBy(operator);
        appUser.setUpdateTime(new Date());
        applicationUserService.deleteById(appUser.getId());
        return Responses.success();
    }

    @Override
    public void clean() {
        super.clean();
        applicationUserService = null;
        appId = null;
        userService = null;
        applicationService = null;
        application = null;
    }

}
