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
package org.joyqueue.handler.routing.command.retry;

import com.google.common.base.Strings;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.CRequest;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.joyqueue.domain.ConsumeRetry;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.handler.Constants;
import org.joyqueue.handler.annotation.Operator;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.util.RetryUtils;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.ApplicationUser;
import org.joyqueue.model.domain.BaseModel;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.Retry;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QRetry;
import org.joyqueue.server.retry.model.RetryQueryCondition;
import org.joyqueue.service.ApplicationUserService;
import org.joyqueue.service.ConsumerService;
import org.joyqueue.service.RetryService;
import org.joyqueue.toolkit.time.SystemClock;
import org.joyqueue.util.LocalSession;
import org.joyqueue.util.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.jd.laf.web.vertx.response.Response.HTTP_BAD_REQUEST;
import static com.jd.laf.web.vertx.response.Response.HTTP_INTERNAL_ERROR;

/**
 * Created by wangxiaofei1 on 2018/12/5.
 */
public class RetryCommand implements Command<Response>, Poolable {
    private Logger logger = LoggerFactory.getLogger(RetryCommand.class);

    @CRequest
    private HttpServerRequest request;
    @Operator
    protected Identity operator;

    @Value(nullable = false)
    private ConsumerService consumerService;

    @Value(nullable = false)
    protected RetryService retryService;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QRetry> qPageQuery) throws Exception {
        if (qPageQuery == null || qPageQuery.getQuery() == null || Strings.isNullOrEmpty(qPageQuery.getQuery().getTopic()) || Strings.isNullOrEmpty(qPageQuery.getQuery().getApp())) {
            return Responses.error(HTTP_BAD_REQUEST,"app,topic,status 不能为空");
        }
        PageResult<ConsumeRetry> pageResult = retryService.findByQuery(qPageQuery);
        return Responses.success(pageResult.getPagination(),pageResult.getResult());
    }

    /**
     * 恢复
     * @param id
     * @return
     * @throws Exception
     */
    @Path("recovery")
    public Response recovery(@QueryParam(Constants.ID) Long id, @QueryParam(Constants.TOPIC)String topic) throws Exception {
        ConsumeRetry retry = retryService.getDataById(id,topic);
        if (retry != null && (retry.getStatus() == Retry.StatusEnum.RETRY_DELETE.getValue() ||
                retry.getStatus() == Retry.StatusEnum.RETRY_OUTOFDATE.getValue()) ||
                retry.getStatus() == Retry.StatusEnum.RETRY_SUCCESS.getValue()) {
            retry.setExpireTime(RetryUtils.getExpireTime().getTime());
            retry.setRetryTime(RetryUtils.getNextRetryTime(new Date(), 0).getTime());
            retry.setRetryCount((short) 0);
            retry.setUpdateTime(SystemClock.now());
            retryService.recover(retry);
            return  Responses.success("恢复成功");
        }
        return Responses.error(HTTP_INTERNAL_ERROR,"恢复失败");
    }

    /**
     * 单个下载
     * @param
     * @return
     * @throws Exception
     */
    @Path("download")
    public void download(@QueryParam(Constants.ID) Long id, @QueryParam(Constants.TOPIC)String topic) throws Exception {
        ConsumeRetry retry = retryService.getDataById(id,topic);
        if (retry != null) {
            HttpServerResponse response = request.response();
            byte[] data = retry.getData();
            if (data.length == 0) {
                throw new JoyQueueException("消息内容为空",HTTP_BAD_REQUEST);
            }
            String fileName = retry.getId() +".txt";
            response.reset();
            BrokerMessage brokerMessage = Serializer.readBrokerMessage(ByteBuffer.wrap(data));
            String message = brokerMessage.getText();
            if (message == null) {
                message = "";
            }
            response.putHeader("Content-Disposition","attachment;fileName=" + fileName)
                    .putHeader("content-type","text/plain")
                    .putHeader("Content-Length",String.valueOf(message.getBytes().length));
            response.write(message,"UTF-8");
            response.end();
        }
    }

    @Path("delete")
    public Response delete(@QueryParam(Constants.ID) Long id, @QueryParam(Constants.TOPIC) String topic) throws Exception {
        ConsumeRetry retry = retryService.getDataById(id,topic);
        retry.setStatus((short) BaseModel.DELETED);
        retry.setUpdateBy(operator.getId().intValue());
        retry.setUpdateTime(SystemClock.now());
        retryService.delete(retry);
        return Responses.success();
    }
    /**
     * 批量删除
     * @param qRetry
     * @return
     * @throws Exception
     */
    @Path("batchDelete")
    public Response batchDelete(@Body QRetry qRetry) throws Exception {

        if ( qRetry == null || Strings.isNullOrEmpty(qRetry.getTopic()) || Strings.isNullOrEmpty(qRetry.getApp())
                || qRetry.getBeginTime() == null ||  qRetry.getEndTime() == null || qRetry.getStatus() == null) {
            return Responses.error(HTTP_BAD_REQUEST,"队列名,消费者,状态,发送开始时间结束时间 不能为空");
        }
        RetryQueryCondition retryQueryCondition = new RetryQueryCondition();
        retryQueryCondition.setTopic(qRetry.getTopic());
        retryQueryCondition.setApp(qRetry.getApp());
        retryQueryCondition.setBusinessId(qRetry.getBusinessId());
        retryQueryCondition.setStatus(qRetry.getStatus().shortValue());
        retryQueryCondition.setStartTime(qRetry.getBeginTime().getTime());
        retryQueryCondition.setEndTime(qRetry.getEndTime().getTime());
        retryService.batchDelete(retryQueryCondition,SystemClock.now(),operator.getId().intValue());
        return Responses.success();
    }
    /**
     * 批量恢复
     * @param ids
     * @return
     * @throws Exception
     */
    @Path("batchRecovery")
    public Response batchRecovery(@QueryParam(Constants.IDS) String ids) throws Exception {
        List<String> idList= Arrays.asList(ids.split(","));
        for (String id : idList) {
//            recovery(Long.valueOf(id));
        }
        return Responses.success();
    }

    /**
     * 重试服务是否可用
     * @return
     * @throws Exception
     */
    @Path("isServerEnabled")
    public Response isServerEnabled() throws Exception {
        return Responses.success(retryService.isServerEnabled());
    }

    private boolean hasSubscribe(String topic, String app) {
        return consumerService.findByTopicAppGroup(null, topic, app, null) != null;
    }

    private boolean hasPrivilege(String app) {
        User user = LocalSession.getSession().getUser();
        if (user == null) {
            return false;
        }
        if (user.getRole() == User.UserRole.ADMIN.value()) {
            return true;
        }
        ApplicationUser applicationUser = applicationUserService.findByUserApp(user.getCode(),app);
        if(applicationUser != null) {
            return true;
        }
        return false;
    }

    @Override
    public Response execute() throws Exception {
        return Responses.error(Response.HTTP_NOT_FOUND,Response.HTTP_NOT_FOUND,"Not Found");
    }

    @Override
    public void clean() {

    }
}
