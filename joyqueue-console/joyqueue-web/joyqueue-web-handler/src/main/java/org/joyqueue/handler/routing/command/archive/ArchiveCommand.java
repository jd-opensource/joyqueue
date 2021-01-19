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
package org.joyqueue.handler.routing.command.archive;

import com.alibaba.fastjson.JSON;
import org.joyqueue.broker.archive.ArchiveUtils;
import org.joyqueue.util.serializer.Serializer;
import org.joyqueue.exception.ServiceException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.message.SourceType;
import org.joyqueue.server.archive.store.utils.ArchiveSerializer;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.handler.Constants;
import org.joyqueue.service.MessagePreviewService;
import org.joyqueue.broker.consumer.MessageConvertSupport;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.model.domain.Archive;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QArchive;
import org.joyqueue.server.archive.store.model.SendLog;
import org.joyqueue.service.ArchiveService;
import org.joyqueue.service.RetryService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static com.jd.laf.web.vertx.response.Response.HTTP_BAD_REQUEST;

/**
 * Created by wangxiaofei1 on 2018/12/7.
 */
public class ArchiveCommand implements Command<Response>, Poolable {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveCommand.class);
    private  final String BATCH_SPLIT="\n\n";
    @Value(nullable = false)
    private ArchiveService archiveService;

    @Value(nullable = false)
    private RetryService retryService;

    @Value(Constants.USER_KEY)
    protected User session;

    @Value(nullable = false)
    private MessagePreviewService messagePreviewService;

    @CRequest
    private HttpServerRequest request;
    private static MessageConvertSupport messageConvertSupport=new MessageConvertSupport();
    @Path("message-types")
    public Response messageTypes() throws Exception {
        return Responses.success(messagePreviewService.getMessageTypeNames());
    }

    /**
     * 分页查询
     * @param qArchive
     * @return
     * @throws Exception
     */
    @Path("search")
    public Response pageQuery(@Body QArchive qArchive) throws Exception {
        if (Strings.isNullOrEmpty(qArchive.getTopic())
                || qArchive.getBeginTime() == null
                || qArchive.getEndTime() == null) {
            return Responses.error(HTTP_BAD_REQUEST,"beginTime,endTime,topic 不能为空");
        }
        archiveService.validate(qArchive);
        return Responses.success(archiveService.findByQuery(qArchive));
    }

    /**
     * 查询详情
     * @param messageId
     * @return
     * @throws Exception
     */
    @Path("consume")
    public Response consume(@QueryParam(Constants.ID) Object messageId) throws Exception {
        return Responses.success(archiveService.findConsumeLog(String.valueOf(messageId),100));
    }

    /**
     * 归档转重试
     * @param archive
     * @return
     * @throws Exception
     */
    @Path("retry")
    public Response archive2retry(@Body Archive archive) throws Exception {
        if (archive == null || archive.getBusinessId() == null
                || archive.getMessageId() == null
                || archive.getSendTime() == null
                || archive.getTopic() == null) {
            return Responses.error(HTTP_BAD_REQUEST,"topic,sendTime,businessId,messageId 不能为空");
        }
        SendLog sendLog = archiveService.findSendLog(archive.getTopic(),archive.getSendTime(),archive.getBusinessId(),archive.getMessageId());
        retryService.add(convertMessageLog(sendLog,archive.getApp()));
        return Responses.success();
    }

    /**
     * 单个下载
     * @param
     * @return
     * @throws Exception
     */
    @Path("download")
    public void download(@QueryParam("businessId") String businessId, @QueryParam("messageId") String messageId
            , @QueryParam("sendTime") String sendTime, @QueryParam("topic") String topic,@QueryParam("messageType") String messageType) throws Exception {
        if (businessId == null
                || messageId == null
                || sendTime == null
                || topic == null) {
            throw new ServiceException(HTTP_BAD_REQUEST, "请求参数错误!");
        }
        HttpServerResponse response = request.response();
        try {
            SendLog sendLog = archiveService.findSendLog(topic, Long.valueOf(sendTime), businessId, messageId);
            if (Objects.nonNull(sendLog)) {
                byte[] data = sendLog.getMessageBody();
                if (data.length == 0) {
                    throw new ServiceException(Response.HTTP_NOT_FOUND,"消息内容为空" );
                }
                String fileName = sendLog.getMessageId() + ".txt";
                response.reset();
                ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                BrokerMessage brokerMessage = Serializer.readBrokerMessage(byteBuffer);
                // Broker message without topic context,we should fill it
                brokerMessage.setTopic(topic);
                // filter target  broker message with index
                brokerMessage = filterBrokerMessage(brokerMessage,sendLog);
                if (Objects.nonNull(brokerMessage)) {
                    String content = preview(brokerMessage, messageType);
                    response.putHeader("Content-Disposition", "attachment;fileName=" + fileName)
                            .putHeader("content-type", "text/plain")
                            .putHeader("Content-Length", String.valueOf(content.getBytes().length));
                    response.write(content, "UTF-8");
                    response.end();
                }else {
                    logger.error("Not found {} message id {},business id {} int batch",topic,messageId,businessId);
                    throw new ServiceException(Response.HTTP_NOT_FOUND, "未找到消息!");
                }
            } else {
                logger.error("Not found {} message id {},business id {} in storage",topic,messageId,businessId);
                throw new ServiceException(Response.HTTP_NOT_FOUND, "未找到消息!");
            }
        }catch (Throwable e){
            if(e instanceof ServiceException){
                ServiceException se=(ServiceException)e;
                response.end(JSON.toJSONString(Responses.error(se.getStatus(),se.getMessage())));
            }else{
                response.end(JSON.toJSONString(Responses.error(ErrorCode.NoTipError.getCode(),e.getMessage())));
            }
        }
    }

    /**
     * Filter Broker Message by index
     * @param sendLog  hex message index
     * @param brokerMessage  may be a batch broker message
     * @return Broker Message or null if not found
     **/
    public BrokerMessage filterBrokerMessage(BrokerMessage brokerMessage, SendLog sendLog) throws GeneralSecurityException {
        List<BrokerMessage> msgs=messageConvertSupport.convert(brokerMessage, SourceType.INTERNAL.getValue());
        if(logger.isDebugEnabled()) {
            logger.debug("send log business id {},message id md5 length {},base 64 bytes {},hex {}", sendLog.getBusinessId(), sendLog.getBytesMessageId().length,
                    Base64.getEncoder().encodeToString(sendLog.getBytesMessageId()), sendLog.getMessageId());
        }
        for(BrokerMessage m:msgs){
            String msgId=ArchiveUtils.messageId(brokerMessage.getTopic(),m.getPartition(),m.getMsgIndexNo());
            byte[] msgIdMd5Bytes= ArchiveSerializer.md5(msgId,null);
            if(logger.isDebugEnabled()) {
                logger.debug("current message business id {},message id {},md5 length {},base 64 bytes {},hex {}", m.getBusinessId(), msgId, msgIdMd5Bytes.length,
                        Base64.getEncoder().encodeToString(msgIdMd5Bytes), ArchiveSerializer.byteArrayToHexStr(msgIdMd5Bytes));
            }
            if(Arrays.equals(msgIdMd5Bytes,sendLog.getBytesMessageId())){
                return m;
            }
        }
        return null;
    }

    /**
     *  Parse broker message to message type
     *
     **/
    public String preview(BrokerMessage brokerMessage,String messageType){
            try {
               return messagePreviewService.preview(messageType, brokerMessage.getDecompressedBody());
            } catch (Throwable e) {
                logger.error("parse error",e);
                return Serializer.readString(brokerMessage.getDecompressedBody());
            }
    }

    @Path("preview")
    public Response preview(@QueryParam("businessId") String businessId, @QueryParam("messageId") String messageId
            , @QueryParam("sendTime") String sendTime, @QueryParam("topic") String topic, @QueryParam("messageType") String messageType) throws Exception {
        try {
            if (businessId == null
                    || messageId == null
                    || sendTime == null
                    || topic == null) {
                throw new ServiceException(HTTP_BAD_REQUEST, "请求参数错误!");
            }
            SendLog sendLog = archiveService.findSendLog(topic, Long.valueOf(sendTime), businessId, messageId);
            if (Objects.nonNull(sendLog)) {
                byte[] data = sendLog.getMessageBody();
                if (data.length == 0) {
                    logger.error("Found {} message id {},business id {} but body is empty", topic, messageId, businessId);
                    throw new ServiceException(Response.HTTP_NOT_FOUND, "消息内容为空!");
                }
                BrokerMessage brokerMessage = Serializer.readBrokerMessage(ByteBuffer.wrap(data));
                brokerMessage.setTopic(topic);
                brokerMessage = filterBrokerMessage(brokerMessage, sendLog);
                if (Objects.nonNull(brokerMessage)) {
                    String content = preview(brokerMessage, messageType);
                    return Responses.success(content);
                } else {
                    logger.error("Not found {} message id {},business id {} int batch", topic, messageId, businessId);
                    throw new ServiceException(Response.HTTP_NOT_FOUND, "未找到消息!");
                }
            } else {
                logger.error("Not found {} message id {},business id {} int storage", topic, messageId, businessId);
                throw new ServiceException(Response.HTTP_NOT_FOUND, "未找到消息!");
            }
        } catch (Throwable e) {
            if (e instanceof ServiceException) {
                ServiceException se = (ServiceException) e;
                return Responses.error(se.getStatus(), se.getMessage());
            } else {
                return Responses.error(ErrorCode.NoTipError.getCode(), e.getMessage());
            }

        }
    }

    /**
     * 归档服务是否可用
     * @return
     * @throws Exception
     */
    @Path("isServerEnabled")
    public Response isServerEnabled() throws Exception {
        return Responses.success(archiveService.isServerEnabled());
    }

    private String convertParams(QArchive qArchive) {
        StringBuilder builder = new StringBuilder();
//        {"beginTime":1551369600000,"businessId":"","count":100,"endTime":1551887999000,"messageId":"","topic":"test_topic"}
        builder.append("{\"beginTime\":").append(qArchive.getBeginTime().getTime()).append(",")
                .append("\"endTime\":").append(qArchive.getEndTime().getTime()).append(",")
                .append("\"count\":").append(qArchive.getCount()).append(",")
                .append("\"messageId\":").append("\"").append(qArchive.getMessageId()).append("\"").append(",")
                .append("\"topic\":").append("\"").append(qArchive.getTopic()).append("\"").append("}");

        return builder.toString();
    }

    private RetryMessageModel convertMessageLog(final SendLog sendLog,String app) throws Exception {
        //sendLog 转brokermessage
//        BrokerMessage brokerMessage = brokerMessageConvert(sendLog);
//        int size = Serializer.sizeOf(brokerMessage);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//        Serializer.write(brokerMessage, byteBuffer,size);
        RetryMessageModel retry = retryConvert(sendLog,app);
//        retry.setBrokerMessage(byteBuffer.array());
        retry.setBrokerMessage(sendLog.getMessageBody());
        return retry;
    }

    private RetryMessageModel retryConvert(final SendLog sendLog,String app){
        RetryMessageModel retry = new RetryMessageModel();
        retry.setApp(app);
        retry.setTopic(sendLog.getTopic());
        retry.setBusinessId(sendLog.getBusinessId());
        retry.setPartition((short) 255);
        return retry;
    }

    @Override
    public Response execute() throws Exception {
        return Responses.error(Response.HTTP_NOT_FOUND,Response.HTTP_NOT_FOUND,"Not Found");
    }

    @Override
    public String type() {
        return "archive";
    }

    @Override
    public void clean() {

    }
}
