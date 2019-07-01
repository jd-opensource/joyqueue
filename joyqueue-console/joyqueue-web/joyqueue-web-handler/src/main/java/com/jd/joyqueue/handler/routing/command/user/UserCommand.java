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
package com.jd.joyqueue.handler.routing.command.user;

import com.jd.joyqueue.handler.annotation.PageQuery;
import com.jd.joyqueue.handler.error.ConfigException;
import com.jd.joyqueue.handler.error.ErrorCode;
import com.jd.joyqueue.handler.routing.command.CommandSupport;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.model.domain.Identity;
import com.jd.joyqueue.model.domain.User;
import com.jd.joyqueue.model.query.QUser;
import com.jd.joyqueue.service.UserService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;


import static com.jd.joyqueue.handler.Constants.CODE;
import static com.jd.joyqueue.handler.Constants.ID;
import static com.jd.joyqueue.handler.Constants.USER_KEY;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public class UserCommand extends CommandSupport<User, UserService, QUser> {
    @Value(USER_KEY)
    protected User operator;

    @Override
    @Path("add")
    public Response add(@Body User model) throws Exception {
        model.setCreateBy(new Identity(-1L));
        model.setUpdateBy(new Identity(-1L));
        super.add(model);
        return Responses.success(model);
    }

    @Override
    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QUser> qPageQuery) throws Exception {
        if (qPageQuery.getQuery()== null) {
            qPageQuery.setQuery(new QUser());
        }
        return super.pageQuery(qPageQuery);
    }

    @Override
    @Path("update")
    public Response update(@QueryParam(ID) Long id, @Body User model) throws Exception {
        if (operator == null || operator.getRole() != User.UserRole.ADMIN.value()) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        return super.update(id, model);
    }

    @Path("getByCode")
    public Response getByCode(@QueryParam(CODE) String code) {
        return Responses.success(service.findByCode(code));
    }


}
