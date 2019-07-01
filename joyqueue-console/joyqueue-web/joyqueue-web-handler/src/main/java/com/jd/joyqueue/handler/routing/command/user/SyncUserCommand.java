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

import com.jd.joyqueue.model.domain.Identity;
import com.jd.joyqueue.model.domain.User;
import com.jd.joyqueue.sync.SyncService;
import com.jd.joyqueue.sync.UserInfo;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import javax.validation.constraints.NotNull;

import static com.jd.joyqueue.handler.Constants.USER_KEY;
import static com.jd.laf.web.vertx.annotation.Body.BodyType.JSON;

/**
 * 同步用户
 * Created by yangyang115 on 18-7-26.
 */
public class SyncUserCommand implements Command<Response>, Poolable {

    //应用服务
    @Value(nullable = false)
    protected SyncService syncService;

    @Body(type = JSON)
    @NotNull
    protected User user;
    @Value(USER_KEY)
    protected User session;

    @Override
    public String type() {
        return "syncUser";
    }

    @Override
    public Response execute() throws Exception {
//        UserInfo info = syncService.syncUser(user);
//        if (info == null) {
//            throw new ConfigException(ErrorCode.UserNotExists);
//        }
        UserInfo info = new UserInfo();
        info.setCode(user.getCode());
        info.setUser(new Identity(session));
        return Responses.success(syncService.addOrUpdateUser(info));
    }

    //  @Override
    public void clean() {
        user = null;
        session = null;
    }
}
