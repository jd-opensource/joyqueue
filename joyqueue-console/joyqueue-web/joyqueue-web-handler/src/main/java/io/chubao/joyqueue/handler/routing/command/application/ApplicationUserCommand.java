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
import io.chubao.joyqueue.handler.routing.command.CommandSupport;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.ApplicationUser;
import io.chubao.joyqueue.model.domain.BaseModel;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.query.QUser;
import io.chubao.joyqueue.service.ApplicationUserService;
import io.chubao.joyqueue.service.UserService;
import io.chubao.joyqueue.sync.SyncService;

import java.util.Date;

import static io.chubao.joyqueue.handler.Constants.APPLICATION;
import static io.chubao.joyqueue.handler.Constants.APP_ID;
import static io.chubao.joyqueue.handler.Constants.USER_ID;


/**
 * Created by wangxiaofei1 on 2018/10/23.
 */
public class ApplicationUserCommand extends CommandSupport<ApplicationUser, UserService, QUser> {
    @QueryParam(APP_ID)
    protected Long appId;//应用id
    @Value(nullable = false)
    protected UserService userService;
    @Value(nullable = false)
    protected SyncService syncService;
    @Value(nullable = false)
    protected ApplicationUserService applicationUserService;
    @Value(APPLICATION)
    protected Application application;

    @Path("add")
    public Response add(@Body ApplicationUser applicationUser) throws Exception {
        //1. 参数检查
        if (applicationUser.getUser() == null) {
            throw new ConfigException(ErrorCode.BadRequest, "没有传入User参数!");
        }
        if(null == application) {
            throw new ConfigException(ErrorCode.BadRequest, "找不到此应用!");
        }
        //2. 查找/同步用户
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
        // 3. 保存appUser
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

    @Path("delete")
    public Response delete(@QueryParam(APP_ID) Long appId, @QueryParam(USER_ID) Long userId) throws Exception {
        ApplicationUser appUser = service.findAppUserByAppIdAndUserId(appId, userId);
        appUser.setStatus(BaseModel.DELETED);
        appUser.setUpdateBy(operator);
        appUser.setUpdateTime(new Date());
        applicationUserService.deleteById(appUser.getId());
        return Responses.success();
    }

}
