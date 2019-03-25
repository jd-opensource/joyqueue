package com.jd.journalq.handler.routing.command.retry;

import com.jd.journalq.common.domain.ConsumeRetry;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.message.BrokerMessage;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.handler.binder.annotation.Operator;
import com.jd.journalq.handler.binder.annotation.Page;
import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.util.RetryUtils;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QConsumer;
import com.jd.journalq.model.query.QRetry;
import com.jd.journalq.service.ApplicationUserService;
import com.jd.journalq.service.ConsumerService;
import com.jd.journalq.service.RetryService;
import com.jd.journalq.toolkit.lang.Strings;
import com.jd.journalq.util.LocalSession;
import com.jd.journalq.util.serializer.Serializer;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.CRequest;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
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
    public Response pageQuery(@Page(typeindex = 0) QPageQuery<QRetry> qPageQuery) throws Exception {
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
    public Response recovery(@ParamterValue(Constants.ID) Object id) throws Exception {
        ConsumeRetry retry = retryService.getDataById(Long.valueOf(String.valueOf(id)));
        if (retry != null && (retry.getStatus() == Retry.StatusEnum.RETRY_DELETE.getValue() ||
                retry.getStatus() == Retry.StatusEnum.RETRY_OUTOFDATE.getValue()) ||
                retry.getStatus() == Retry.StatusEnum.RETRY_SUCCESS.getValue()) {
            retry.setExpireTime(RetryUtils.getExpireTime().getTime());
            retry.setRetryTime(RetryUtils.getNextRetryTime(new Date(), 0).getTime());
            retry.setRetryCount((short) 0);
            retry.setUpdateTime(System.currentTimeMillis());
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
    public void download(@ParamterValue(Constants.ID) Object id) throws Exception {
        ConsumeRetry retry = retryService.getDataById(Long.valueOf(String.valueOf(id)));
        if (retry != null) {
            HttpServerResponse response = request.response();
            byte[] data = retry.getData();
            if (data.length == 0) {
                throw new JMQException("消息内容为空",HTTP_BAD_REQUEST);
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
    public Response delete(@ParamterValue(Constants.ID) Object id) throws Exception {
        ConsumeRetry retry = retryService.getDataById(Long.valueOf(String.valueOf(id)));
        retry.setStatus((short) BaseModel.DELETED);
        retry.setUpdateBy(operator.getId().intValue());
        retry.setUpdateTime(System.currentTimeMillis());
        retryService.delete(retry);
        return Responses.success();
    }
    /**
     * 批量删除
     * @param ids
     * @return
     * @throws Exception
     */
    @Path("batchDelete")
    public Response batchDelete(@ParamterValue(Constants.IDS) Object ids) throws Exception {
        List<String> idList= Arrays.asList(String.valueOf(ids).split(","));
        for (String id : idList) {
            delete(id);
        }
        return Responses.success();
    }
    /**
     * 批量恢复
     * @param ids
     * @return
     * @throws Exception
     */
    @Path("batchRecovery")
    public Response batchRecovery(@ParamterValue(Constants.IDS) Object ids) throws Exception {
        List<String> idList= Arrays.asList(String.valueOf(ids).split(","));
        for (String id : idList) {
            recovery(id);
        }
        return Responses.success();
    }

    private boolean hasSubscribe(String topic,String app) {
        QConsumer qConsumer =  new QConsumer();
        qConsumer.setApp(new Identity(app));
        qConsumer.setTopic(new Topic(topic));
        List<Consumer> consumerList = null;
        try {
            consumerList = consumerService.findByQuery(qConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (consumerList != null && consumerList.size() > 0) {
            return true;
        }
        return false;
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
