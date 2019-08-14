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
package io.chubao.joyqueue.handler.routing.command.archive;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;
import io.chubao.joyqueue.util.serializer.Serializer;
import io.chubao.joyqueue.handler.Constants;
import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.model.domain.Archive;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.query.QArchive;
import io.chubao.joyqueue.server.archive.store.model.SendLog;
import io.chubao.joyqueue.service.ArchiveService;
import io.chubao.joyqueue.service.RetryService;
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
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static com.jd.laf.web.vertx.response.Response.HTTP_BAD_REQUEST;

/**
 * Created by wangxiaofei1 on 2018/12/7.
 */
public class ArchiveCommand implements Command<Response>, Poolable {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveCommand.class);

    @Value(nullable = false)
    private ArchiveService archiveService;

    @Value(nullable = false)
    private RetryService retryService;

    @Value(Constants.USER_KEY)
    protected User session;

    @CRequest
    private HttpServerRequest request;

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
        retryService.add(convertMessageLog(sendLog));
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
            , @QueryParam("sendTime") String sendTime, @QueryParam("topic") String topic) throws Exception {
        if (businessId == null
                || messageId == null
                || sendTime == null
                || topic == null) {
            return;
        }
        SendLog sendLog = archiveService.findSendLog(topic,Long.valueOf(sendTime),businessId,messageId);
        if (sendLog != null) {
            HttpServerResponse response = request.response();
            byte[] data = sendLog.getMessageBody();
            if (data.length == 0) {
                throw new JoyQueueException("消息内容为空",HTTP_BAD_REQUEST);
            }
            String fileName = sendLog.getMessageId() +".txt";
            response.reset();
            BrokerMessage brokerMessage = Serializer.readBrokerMessage(ByteBuffer.wrap(data));
//            Message message = new Message();
//            message.setBody(data);
//            message.setCompressed(true);
//            message.setCompressionType(Message.CompressionType.valueOf(sendLog.getCompressType()));

            String messageStr = null;
            try {
                messageStr = brokerMessage.getText();
            } catch (Exception e) {
                messageStr = Bytes.toString(data);
            }

            if (messageStr == null) {
                messageStr = "";
            }
            response.putHeader("Content-Disposition","attachment;fileName=" + fileName)
                    .putHeader("content-type","text/plain")
                    .putHeader("Content-Length",String.valueOf(messageStr.getBytes().length));
            response.write(messageStr,"UTF-8");
            response.end();
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

    private RetryMessageModel convertMessageLog(final SendLog sendLog) throws Exception {
        //sendLog 转brokermessage
//        BrokerMessage brokerMessage = brokerMessageConvert(sendLog);
//        int size = Serializer.sizeOf(brokerMessage);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//        Serializer.write(brokerMessage, byteBuffer,size);
        RetryMessageModel retry = retryConvert(sendLog);
//        retry.setBrokerMessage(byteBuffer.array());
        retry.setBrokerMessage(sendLog.getMessageBody());
        return retry;
    }


//    private BrokerMessage brokerMessageConvert(final SendLog sendLog) {
//        BrokerMessage brokerMessage = new BrokerMessage();
//        brokerMessage.setBusinessId(sendLog.getBusinessId());
//        brokerMessage.setApp(sendLog.getApp());
//        brokerMessage.setTopic(sendLog.getTopic());
//        brokerMessage.setCompressed(Message.CompressionType.valueOf(sendLog.getCompressType()) == Message.CompressionType.Zip);
//        brokerMessage.setCompressionType(Message.CompressionType.valueOf(sendLog.getCompressType()));
//        brokerMessage.setBody(sendLog.getMessageBody());
//        brokerMessage.setClientIp(sendLog.getClientIp());
//        return brokerMessage;
//    }

    private RetryMessageModel retryConvert(final SendLog sendLog){
        RetryMessageModel retry = new RetryMessageModel();
        retry.setApp(sendLog.getApp());
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
