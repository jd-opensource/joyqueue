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
package org.joyqueue.handler.routing.command.monitor;

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
import org.joyqueue.domain.Broker;
import org.joyqueue.handler.Constants;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.model.BrokerMetadata;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.BrokerClient;
import org.joyqueue.model.domain.BrokerMonitorInfoWithDC;
import org.joyqueue.model.domain.BrokerMonitorRecord;
import org.joyqueue.model.domain.BrokerTopicMonitor;
import org.joyqueue.model.domain.ConnectionMonitorInfoWithIp;
import org.joyqueue.model.domain.ProducerSendMessage;
import org.joyqueue.model.domain.SimplifiedBrokeMessage;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QMonitor;
import org.joyqueue.model.query.QPartitionGroupMonitor;
import org.joyqueue.monitor.BrokerMessageInfo;
import org.joyqueue.monitor.BrokerMonitorInfo;
import org.joyqueue.monitor.BrokerStartupInfo;
import org.joyqueue.monitor.Client;
import org.joyqueue.service.*;
import org.joyqueue.toolkit.io.Directory;
import org.joyqueue.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

import static com.jd.laf.web.vertx.response.Response.HTTP_NOT_FOUND;
import static org.joyqueue.handler.error.ErrorCode.NoTipError;

public class BrokerMonitorCommand implements Command<Response>, Poolable {
    private static final Logger logger = LoggerFactory.getLogger(BrokerMonitorCommand.class);

    @Value
    private BrokerMonitorService brokerMonitorService;

    @Value
    private BrokerMessageService brokerMessageService;

    @Value
    private BrokerManageService brokerManageService;
    @Value
    private CoordinatorMonitorService coordinatorMonitorService;

    @Value
    private BrokerTopicMonitorService brokerTopicMonitorService;

    @Value
    private ApplicationUserService applicationUserService;

    @Value
    private BrokerService brokerService;

    @Value
    private DataCenterService dataCenterService;

    @Value(Constants.USER_KEY)
    protected User session;

    public static final String DATA_CENTER_IP_SEPARATOR = ":";

    @Override
    public Response execute() throws Exception {
        return Responses.error(HTTP_NOT_FOUND, HTTP_NOT_FOUND,"Not Found");
    }

    /**
     *关闭生产者或消费者broker的连接
     *
     */
    @Path("removeConnections")
    public Response removeConnections(@Body Subscribe subscribe, @QueryParam("id") Long id) {
        Integer brokerId = id.intValue();
        if(brokerMonitorService.removeBrokerMonitorConnections(subscribe,brokerId))
            return Responses.success("success");
        return Responses.error(500,"broker not found, operation error");
    }
    /**
     * topic and app 生产或者消费监控汇总信息
     *
     */
    @Path("find")
    public Response find(@Body Subscribe subscribe){
        BrokerMonitorRecord record;
        try {
            record = brokerMonitorService.find(subscribe, true);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
        return Responses.success(record);
    }

    /**
     * 指定topic、app 在partition 上的生产或者消费监控
     *
     **/
    @Path("findOnPartition")
    public Response findMonitorOnPartition(@Body Subscribe subscribe) {
        try {
            List<BrokerMonitorRecord> record = brokerMonitorService.findMonitorOnPartition(subscribe);
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定topic、app 在partition groups 上的生产或者消费监控
     *
     **/
    @Path("findOnPartitionGroups")
    public Response findMonitorOnPartitionGroups(@Body Subscribe subscribe) {
        try {
            List<BrokerMonitorRecord> record=brokerMonitorService.findMonitorOnPartitionGroupsForTopicApp(subscribe);
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定topic、app 在broker 上的生产或者消费监控
     *
     **/
    @Path("findOnBroker")
    public Response findMonitorOnBroker(@Body Subscribe subscribe) {
        try {
            List<BrokerMonitorRecord> record = brokerMonitorService.findMonitorOnBroker(subscribe);
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    @Path("findTopicCount")
    public Response findTopicCount(@QueryParam("brokerId") Long brokerId) throws Exception {
        List<String> topicList;
        try {
            topicList = brokerTopicMonitorService.queryTopicList(brokerId);
            if (CollectionUtils.isEmpty(topicList)) {
                return Responses.success(0);
            }
            return Responses.success(topicList.size());
        }catch (Exception e) {
            logger.error("",e);
            return Responses.error(500, "brokerId: "+brokerId + "find topicCount cause error");
        }
    }

    /**
     * 指定topic、app 在broker 上的连接数
     *
     **/
    @Path("findConnectionOnBroker")
    public Response findConnectionOnBroker(@Body Subscribe subscribe) {
        try {
            List<ConnectionMonitorInfoWithIp> records = brokerMonitorService.findConnectionOnBroker(subscribe);
            if (records != null) {
                records.forEach(record -> {
                    try {
                        record.setDataCenter(dataCenterService.findByIp(record.getIp().split(DATA_CENTER_IP_SEPARATOR)[0]));
                    } catch (Exception e) {
                        logger.error(String.format("find data center by ip error. ip is %s", record.getIp()), e);
                    }
                });
            }
            return Responses.success(records);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }

    }

    /**
     * 指定topic、app 生产或者消费Client监控汇总信息
     *
     **/
    @Path("findClient")
    public Response findClients(@Body Subscribe subscribe){
        try {
            List<BrokerClient> record = brokerMonitorService.findClients(subscribe);
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定topic、app、broker 生产或者消费 监控
     *
     **/
    @Path("brokerMonitor")
    public Response brokerMonitor(@PageQuery QPageQuery<QMonitor> qPageQuery){
        try {
            PageResult<BrokerTopicMonitor> pageResult = brokerTopicMonitorService.queryTopicsMointor(qPageQuery);
            return new Response(pageResult.getResult(),pageResult.getPagination());
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定topic、app、broker 生产或者消费 partitionGroup 监控
     *
     **/
    @Path("partitionGroupMonitor")
    public Response partitionGroupMonitor(@PageQuery QPageQuery<QMonitor> qPageQuery){
        try {
            List<BrokerTopicMonitor> brokerTopicMonitors = brokerTopicMonitorService.queryTopicsPartitionMonitors(Integer.valueOf(String.valueOf(qPageQuery.getQuery().getBrokerId())));
            PageResult<BrokerTopicMonitor> pageResult = new PageResult<>();
            pageResult.setResult(brokerTopicMonitors);
            pageResult.setPagination(new Pagination(0, brokerTopicMonitors.size() - 1));
            return new Response(pageResult.getResult(), pageResult.getPagination());
        } catch (Exception e) {
        logger.error("query broker monitor info error.", e);
        return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定broker 连接 监控
     *
     **/
    @Path("brokerConnectionsMonitor")
    public Response brokerConnectionsMonitor(@PageQuery QPageQuery<QMonitor> qPageQuery){
        try {
            PageResult<Client> pageResult = brokerTopicMonitorService.queryClientConnectionDetail(qPageQuery);
            return new Response(pageResult.getResult(), pageResult.getPagination());
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }


    /**
     * 指定topic、app 生产或者消费 partition groups监控汇总信息
     *
     **/
    @Path("findMonitorOnPartitionGroupsForTopicApp")
    public Response findMonitorOnPartitionsGroupsForTopicApp(@Body Subscribe subscribe){
        try {
            List<BrokerMonitorRecord> record=brokerMonitorService.findMonitorOnPartitionGroupsForTopicApp(subscribe);
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定topic、app 生产或者消费 partition groups监控汇总信息
     *
     **/
    @Path("findMonitorOnPartitionGroupDetailForTopicApp")
    public Response findMonitorOnPartitionGroupDetailForTopicApp(@Body QPartitionGroupMonitor partitionGroupMonitor){
        try {
            List<BrokerMonitorRecord> record = brokerMonitorService.findMonitorOnPartitionGroupDetailForTopicApp(partitionGroupMonitor.getSubscribe(), partitionGroupMonitor.getPartitionGroup());
            record.sort(Comparator.comparingInt(BrokerMonitorRecord::getPartition));
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }
    /**
     * 指定topic、app 生产或者消费Client监控汇总信息
     *
     **/
    @Path("previewMessage")
    public Response previewMessage(@Body Subscribe subscribe, @QueryParam("messageDecodeType") String messageType){
        int defaultCount=10;

        List<SimplifiedBrokeMessage> messages=brokerMessageService.previewMessage(subscribe,messageType,defaultCount);
        return Responses.success(messages);
    }

    /**
     * 指定topic、app 生产或者消费Client监控汇总信息
     *
     **/
    @Path("viewMessage")
    public Response viewMessage(@Body Subscribe subscribe,@QueryParam("partition") String partition, @QueryParam("index")String index,
                                @QueryParam("timestamp")String timestamp,@QueryParam("messageDecodeType") String messageDecodeType){
        int defaultCount=10;
        if (StringUtils.isNotEmpty(timestamp) && StringUtils.isEmpty(index)){
            Long indexByTime = brokerMessageService.getPartitionIndexByTime(subscribe,partition,timestamp);
            index = String.valueOf(indexByTime);
        }
        List<BrokerMessageInfo> messages=brokerMessageService.viewMessage(subscribe,messageDecodeType,partition,index,defaultCount);
        return Responses.success(messages);
    }

    /**
     * 指定 broker archive汇总信息
     * @param broker  target broker
     **/
    @Path("archiveState")
    public Response archiveState(@Body Broker broker){
        try {
            return Responses.success(brokerMonitorService.findArchiveState(broker.getIp(),broker.getMonitorPort()));
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }


    /**
     * 指定 app 的协调者信息
     * @param subscribe
     **/
    @Path("coordinator")
    public Response coordinator(@Body Subscribe subscribe){
        try {
            if(NullUtil.isEmpty(subscribe.getApp())){ return  Responses.error(ErrorCode.BadRequest.getCode(),"app 不能为空");}
            return Responses.success(coordinatorMonitorService.findCoordinatorInfo(subscribe));
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定 app,topic 消费组member
     * @param subscribe
     **/
    @Path("coordinatorGroupMember")
    public Response coordinatorGroupMember(@Body Subscribe subscribe){
        try {
            if (NullUtil.isEmpty(subscribe.getApp()) || NullUtil.isEmpty(subscribe.getTopic())) {
                return Responses.error(ErrorCode.BadRequest.getCode(), "app 和 topic 不能为空");
            }
            return Responses.success(coordinatorMonitorService.findCoordinatorGroupMember(subscribe));
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定 app 的协调者信息
     * @param subscribe
     **/
    @Path("coordinatorGroupExpiredMember")
    public Response coordinatorGroupExpiredMember(@Body Subscribe subscribe){
        try {
            if (NullUtil.isEmpty(subscribe.getApp()) || NullUtil.isEmpty(subscribe.getTopic())) {
                return Responses.error(ErrorCode.BadRequest.getCode(), "app 和 topic 不能为空");
            }
            return Responses.success(coordinatorMonitorService.findExpiredCoordinatorGroupMember(subscribe));
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 获取详情
     * @param partitionGroupMonitor
     * @return
     */
    @Path("findPartitionGroupMetric")
    public Response findPartitionGroupMetric(@Body QPartitionGroupMonitor partitionGroupMonitor){
        try {
            if (NullUtil.isEmpty(partitionGroupMonitor.getPartitionGroup()) || NullUtil.isEmpty(partitionGroupMonitor.getSubscribe())) {
                return Responses.error(ErrorCode.BadRequest.getCode(), "app 和 topic 不能为空");
            }
            Subscribe subscribe = partitionGroupMonitor.getSubscribe();
            int groupNo = partitionGroupMonitor.getPartitionGroup();
            return Responses.success(brokerMonitorService.findPartitionGroupMetric(subscribe.getNamespace().getCode(),subscribe.getTopic().getCode(),groupNo));
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * broker监控
     * @param brokerId
     * @return
     */
    @Path("findBrokerDetail")
    public Response findBrokerDetail(@QueryParam("brokerId") Integer brokerId) throws Exception {
        BrokerMonitorInfo info = brokerTopicMonitorService.findBrokerMonitor(Long.valueOf(brokerId));
        if (info == null) {
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), "Not found broker detail monitor info. ");
        }
        BrokerMonitorInfoWithDC infoWithDC = new BrokerMonitorInfoWithDC(info);
        org.joyqueue.model.domain.Broker broker = brokerService.findById(brokerId);
        if (broker == null || StringUtils.isEmpty(broker.getIp())) {
            logger.error(String.format("Not Found broker. id is %s. ", brokerId));
            return Responses.success(info);
        }
        try {
            infoWithDC.setDataCenter(dataCenterService.findByIp(broker.getIp().split(DATA_CENTER_IP_SEPARATOR)[0]));
        } catch (Exception e) {
            logger.error(String.format("Found data center error. broker id is %s. ", brokerId), e);
            return Responses.success(info);
        }

        return Responses.success(infoWithDC);
    }

    /**
     * broker启动信息
     * @param brokerId
     * @return
     */
    @Path("startInfo")
    public Response startInfo(@QueryParam("brokerId") Long brokerId) throws Exception {
        try {
            BrokerStartupInfo brokerStartupInfo = brokerTopicMonitorService.getStartupInfo(brokerId);
            return Responses.success(brokerStartupInfo);
        } catch (Exception e) {
            logger.error("query broker start info error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }


    /**
     * broker启动信息
     * @param brokerId
     * @return
     */
    @Path("storeTreeView")
    public Response storeTreeView(@QueryParam("brokerId") Integer brokerId,@QueryParam("recursive") boolean recursive) throws Exception {
        try {
            Directory directory = brokerManageService.storeTreeView(brokerId,recursive);
            return Responses.success(directory);
        } catch (Exception e) {
            logger.error("query broker store tree view error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * broker启动信息
     * @param brokerId
     * @return
     */
    @Path("garbageFile")
    public Response  deleteGarbageFile(@QueryParam("brokerId") Integer brokerId,
                                       @QueryParam("fileName") String fileName,
                                       @QueryParam("retain") boolean retain) throws Exception {
        try {
            boolean result = brokerManageService.deleteGarbageFile(brokerId,fileName,retain);
            return Responses.success(result);
        } catch (Exception e) {
            logger.error("query broker store tree view error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * TODO 改成post方法,topicFullName可能存在
     * broker启动信息
     * @param brokerId
     * @return
     */
    @Path("findBrokerMetadata")
    public Response findBrokerMetadata(@QueryParam("brokerId") Long brokerId, @QueryParam("topicFullName") String topicFullName,
                                       @QueryParam("group") Integer group) throws Exception {
        org.joyqueue.model.domain.Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(brokerId)));
        if (broker == null) {
            String msg = String.format("can not find broker with id %s", brokerId);
            logger.error(msg);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), msg);
        }

        try {
            BrokerMetadata brokerMetadata = brokerMonitorService.findBrokerMetadata(broker, topicFullName, group);
            return Responses.success(brokerMetadata);
        } catch (Exception e) {
            logger.error("query broker metadata error.", e);
            return Responses.error(NoTipError.getCode(), NoTipError.getStatus(), e.getMessage());
        }
    }

    @Path("sendMessage")
    public Response sendMessage(@Body ProducerSendMessage sendMessage) {
        if (session.getRole() != User.UserRole.ADMIN.value()) {
            if (applicationUserService.findByUserApp(session.getCode(), sendMessage.getApp()) == null) {
                return Responses.error(ErrorCode.BadRequest.getCode(), ErrorCode.BadRequest.getCode(), "bad request");
            }
        }

        brokerMessageService.sendMessage(sendMessage);
        return Responses.success("test");
    }

    @Override
    public void clean() {
        brokerMonitorService = null;
        brokerMessageService = null;
        coordinatorMonitorService = null;
    }
}
