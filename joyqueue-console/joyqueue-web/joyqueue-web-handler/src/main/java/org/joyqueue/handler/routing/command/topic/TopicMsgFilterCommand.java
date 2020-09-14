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
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.domain.TopicName;
import org.joyqueue.handler.Constants;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.ApplicationToken;
import org.joyqueue.model.domain.Consumer;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.TopicMsgFilter;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QTopicMsgFilter;
import org.joyqueue.service.ApplicationTokenService;
import org.joyqueue.service.ConsumerService;
import org.joyqueue.service.TopicMsgFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
public class TopicMsgFilterCommand implements Command<Response>, Poolable {

    private static final Logger logger = LoggerFactory.getLogger(TopicMsgFilterCommand.class);

    @Value(Constants.USER_KEY)
    protected User session;

    @Value
    private TopicMsgFilterService topicMsgFilterService;

    @Value
    private ConsumerService consumerService;

    @Value
    private ApplicationTokenService applicationTokenService;

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
        return Responses.success(result.getPagination(), result.getResult());
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

    @Path("validateAppToken")
    public Response validateAppToken(@QueryParam("app") String app, @QueryParam("topic") String topic,@QueryParam("token") String token) throws Exception {
        String subscribeGroup ="";
        if (app.contains(".")) {
            int idx = app.indexOf('.');
            subscribeGroup = app.substring(idx + 1);
            app = app.substring(0, idx);
        }
        try {
            TopicName topicName = TopicName.parse(topic);
            Consumer consumer = consumerService.findByTopicAppGroup(topicName.getNamespace(), topicName.getCode(), app, subscribeGroup);
            if (consumer != null) {
                List<ApplicationToken> appTokens = applicationTokenService.findByApp(app);
                if (CollectionUtils.isNotEmpty(appTokens)) {
                    Date date = new Date();
                    List<ApplicationToken> collect = appTokens.stream()
                            .filter(appToken -> appToken.getToken().equals(token) && appToken.getExpirationTime().after(date)
                                    && appToken.getEffectiveTime().before(date)).collect(Collectors.toList());
                    if (collect.size() > 0){
                        return Responses.success();
                    }
                }
                logger.error("token: {} is not found or invalid", token);
                return Responses.error(404,"token: "+token+" is not found or invalid");
            } else {
                if (StringUtils.isNotBlank(subscribeGroup)) {
                    logger.error("app: {} not found or subscribeGroup: {} not found or don't related with topic: {}",app, subscribeGroup, topic);
                    return Responses.error(404,"app: "+app+" not found or subscribeGroup: "+subscribeGroup+" not found or don't related with topic: "+topic);
                } else {
                    logger.error("app: {} not found or don't related with topic: {}", app, topic);
                    return Responses.error(404, "app: " + app + " not found or don't related with topic: " + topic);
                }
            }
        } catch (NullPointerException e) {
            logger.error("topic not found or doesn't have related app");
            return Responses.error(404,"topic not found or doesn't have related app");
        }
    }

    @Override
    public void clean() {
        topicMsgFilterService = null;
    }

    private TopicMsgFilter filterConvert(QTopicMsgFilter filter) {
        TopicMsgFilter msgFilter = new TopicMsgFilter();
        if (filter.getPartition() != null && filter.getPartition() >= 0) {
            msgFilter.setPartition(filter.getPartition());
        } else {
            msgFilter.setPartition(-1);
        }
        msgFilter.setApp(filter.getApp());
        msgFilter.setToken(filter.getToken());
        msgFilter.setMsgFormat(filter.getMsgFormat());
        msgFilter.setQueryCount(filter.getQueryCount());
        msgFilter.setTotalCount(filter.getTotalCount());
        msgFilter.setTopic(filter.getTopic());
        msgFilter.setFilter(filter.getFilter());
        msgFilter.setCreateTime(new Date());
        msgFilter.setCreateBy(new Identity(filter.getUserId(),filter.getUserCode()));
        if (filter.getOffsetStartTime() > 0) {
            msgFilter.setOffsetStartTime(new Date(filter.getOffsetStartTime()));
            msgFilter.setOffsetEndTime(new Date(filter.getOffsetEndTime()));
        } else {
            msgFilter.setOffset(filter.getOffset());
        }
        msgFilter.setUpdateBy(new Identity(filter.getUserId(),filter.getUserCode()));
        msgFilter.setUpdateTime(new Date());
        return msgFilter;
    }
}
