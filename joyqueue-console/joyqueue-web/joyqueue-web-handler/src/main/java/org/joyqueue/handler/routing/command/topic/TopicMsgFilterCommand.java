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
package org.joyqueue.handler.routing.command.topic;

import com.google.common.base.Preconditions;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.handler.Constants;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.TopicMsgFilter;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QTopicMsgFilter;
import org.joyqueue.service.TopicMsgFilterService;

import java.util.Date;

/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
public class TopicMsgFilterCommand implements Command<Response>, Poolable {

    @Value(Constants.USER_KEY)
    protected User session;

    @Value
    private TopicMsgFilterService topicMsgFilterService;

    @Path("findTopicMsgFilters")
    public Response findTopicMsgFilters(@Body @PageQuery QPageQuery<QTopicMsgFilter> qPageQuery) throws Exception {
        Preconditions.checkArgument(qPageQuery != null, "Illegal args.");
        if (qPageQuery.getQuery() != null) {
            qPageQuery.getQuery().setUserId(session.getId());
            qPageQuery.getQuery().setUserName(session.getName());
            qPageQuery.getQuery().setUserCode(session.getCode());
            qPageQuery.getQuery().setRole(session.getRole());
            qPageQuery.getQuery().setAdmin(session.getRole() == User.UserRole.ADMIN.value() ? Boolean.TRUE : Boolean.FALSE);
        }
        PageResult<TopicMsgFilter> result = topicMsgFilterService.findTopicMsgFilters(qPageQuery);
        return Responses.success(result.getPagination(),result.getResult());
    }

    @Path("msgFilter")
    public Response filter(@Body QTopicMsgFilter msgFilter) throws Exception {
        Preconditions.checkArgument(msgFilter != null, "Illegal args.");
        msgFilter.setUserId(session.getId());
        msgFilter.setUserName(session.getName());
        msgFilter.setUserCode(session.getCode());
        msgFilter.setRole(session.getRole());
        topicMsgFilterService.execute(filterConvert(msgFilter));
        return Responses.success();
    }

    @Path("addTopicMsgFilter")
    public Response addTopicMsgFilter(@Body QTopicMsgFilter msgFilter) throws Exception {
        Preconditions.checkArgument(msgFilter != null, "Illegal args.");
        msgFilter.setUserId(session.getId());
        msgFilter.setUserName(session.getName());
        msgFilter.setUserCode(session.getCode());
        msgFilter.setRole(session.getRole());
        topicMsgFilterService.add(filterConvert(msgFilter));
        return Responses.success();
    }

    @Override
    public void clean() {
        topicMsgFilterService = null;
    }

    private TopicMsgFilter filterConvert(QTopicMsgFilter filter) {
        TopicMsgFilter msgFilter = new TopicMsgFilter();
        msgFilter.setApp(filter.getApp());
        msgFilter.setTopic(filter.getTopic());
        msgFilter.setFilter(filter.getFilter());
        msgFilter.setCreateTime(new Date(filter.getQueryTime()));
        msgFilter.setCreateBy(new Identity(filter.getUserId(),filter.getUserCode()));
        msgFilter.setOffsetTime(new Date(filter.getTimestamp()));
        msgFilter.setUserCode(filter.getUserCode());
        msgFilter.setUserId(filter.getUserId());
//        msgFilter.setDescription(filter.getDescription());
        return msgFilter;
    }
}
