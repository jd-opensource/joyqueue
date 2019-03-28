package com.jd.journalq.handler.routing.command.monitor;

import com.jd.journalq.domain.Broker;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.monitor.Client;
import com.jd.journalq.handler.binder.annotation.Body;
import com.jd.journalq.handler.binder.annotation.Page;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.binder.BodyType;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QMonitor;
import com.jd.journalq.model.query.QPartitionGroupMonitor;
import com.jd.journalq.service.BrokerMessageService;
import com.jd.journalq.service.BrokerMonitorService;
import com.jd.journalq.service.BrokerTopicMonitorService;
import com.jd.journalq.service.CoordinatorMonitorService;
import com.jd.journalq.util.NullUtil;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BrokerMonitorCommand implements Command<Response>, Poolable {
    private static final Logger logger = LoggerFactory.getLogger(BrokerMonitorCommand.class);

    @Value
    private BrokerMonitorService brokerMonitorService;

    @Value
    private BrokerMessageService  brokerMessageService;

    @Value
    CoordinatorMonitorService coordinatorMonitorService;
    @Value
    private BrokerTopicMonitorService brokerTopicMonitorService;

    @Override
    public Response execute() throws Exception {
        return Responses.error(Response.HTTP_NOT_FOUND,Response.HTTP_NOT_FOUND,"Not Found");
    }

    /**
     * topic and app 生产或者消费监控汇总信息
     *
     */
    @Path("find")
    public Response find(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        BrokerMonitorRecord record;
        try {
            record = brokerMonitorService.find(subscribe, true);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
        return Responses.success(record);
    }

    /**
     * 指定topic、app 在partition 上的生产或者消费监控
     *
     **/
    @Path("findOnPartition")
    public Response findMonitorOnPartition(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe) {

        List<BrokerMonitorRecord> record = brokerMonitorService.findMonitorOnPartition(subscribe);
        return Responses.success(record);
    }

    /**
     * 指定topic、app 在partition groups 上的生产或者消费监控
     *
     **/
    @Path("findOnPartitionGroups")
    public Response findMonitorOnPartitionGroups(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe) {

        List<BrokerMonitorRecord> record=brokerMonitorService.findMonitorOnPartitionGroupsForTopicApp(subscribe,false);
        return Responses.success(record);
    }

    /**
     * 指定topic、app 在broker 上的生产或者消费监控
     *
     **/
    @Path("findOnBroker")
    public Response findMonitorOnBroker(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe) {

        List<BrokerMonitorRecord> record= brokerMonitorService.findMonitorOnBroker(subscribe,true);
        return Responses.success(record);
    }

    /**
     * 指定topic、app 在broker 上的连接数
     *
     **/
    @Path("findConnectionOnBroker")
    public Response findConnectionOnBroker(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe) {
        List<ConnectionMonitorInfoWithIp> record=brokerMonitorService.findConnectionOnBroker(subscribe);
        return Responses.success(record);
    }

    /**
     * 指定topic、app 生产或者消费Client监控汇总信息
     *
     **/
    @Path("findClient")
    public Response findClients(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        List<BrokerClient> record=brokerMonitorService.findClients(subscribe);
        return Responses.success(record);
    }

    /**
     * 指定topic、app、broker 生产或者消费 监控
     *
     **/
    @Path("brokerMonitor")
    public Response brokerMonitor(@Page(typeindex = 0)QPageQuery<QMonitor> qPageQuery){
        PageResult<BrokerTopicMonitor> pageResult = brokerTopicMonitorService.queryTopicsMointor(qPageQuery);
        return new Response(pageResult.getResult(),pageResult.getPagination());
    }

    /**
     * 指定topic、app、broker 生产或者消费 partitionGroup 监控
     *
     **/
    @Path("partitionGroupMonitor")
    public Response partitionGroupMonitor(@Page(typeindex = 0)QPageQuery<QMonitor> qPageQuery){
        PageResult<BrokerTopicMonitor> pageResult = brokerTopicMonitorService.queryTopicsPartitionMointor(qPageQuery);
        return new Response(pageResult.getResult(),pageResult.getPagination());
    }

    /**
     * 指定broker 连接 监控
     *
     **/
    @Path("brokerConnectionsMonitor")
    public Response brokerConnectionsMonitor(@Page(typeindex = 0)QPageQuery<QMonitor> qPageQuery){
        PageResult<Client> pageResult = brokerTopicMonitorService.queryClientConnectionDetail(qPageQuery);
        return new Response(pageResult.getResult(),pageResult.getPagination());
    }


    /**
     * 指定topic、app 生产或者消费 partition groups监控汇总信息
     *
     **/
    @Path("findMonitorOnPartitionGroupsForTopicApp")
    public Response findMonitorOnPartitionsGroupsForTopicApp(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        List<BrokerMonitorRecord> record=brokerMonitorService.findMonitorOnPartitionGroupsForTopicApp(subscribe,false);
        return Responses.success(record);
    }

    /**
     * 指定topic、app 生产或者消费 partition groups监控汇总信息
     *
     **/
    @Path("findMonitorOnPartitionGroupDetailForTopicApp")
    public Response findMonitorOnPartitionGroupDetailForTopicApp(@Body(typeindex = 0,type = BodyType.JSON) QPartitionGroupMonitor partitionGroupMonitor){
        List<BrokerMonitorRecord> record=brokerMonitorService.findMonitorOnPartitionGroupDetailForTopicApp(partitionGroupMonitor.getSubscribe(),partitionGroupMonitor.getPartitionGroup());
        return Responses.success(record);
    }
    /**
     * 指定topic、app 生产或者消费Client监控汇总信息
     *
     **/
    @Path("previewMessage")
    public Response previewMessage(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        int defaultCount=10;
        List<SimplifiedBrokeMessage> messages=brokerMessageService.previewPendingMessage(subscribe,defaultCount);
        return Responses.success(messages);
    }

    /**
     * 指定 broker archive汇总信息
     * @param broker  target broker
     **/
    @Path("archiveState")
    public Response archiveState(@Body(typeindex = 0,type = BodyType.JSON) Broker broker){
        return Responses.success(brokerMonitorService.findArchiveState(broker.getIp(),broker.getMonitorPort()));
    }


    /**
     * 指定 app 的协调者信息
     * @param subscribe
     **/
    @Path("coordinator")
    public Response coordinator(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        if(NullUtil.isEmpty(subscribe.getApp())){ return  Responses.error(ErrorCode.BadRequest.getCode(),"app 不能为空");}
        return Responses.success(coordinatorMonitorService.findCoordinatorInfo(subscribe));
    }

    /**
     * 指定 app,topic 消费组member
     * @param subscribe
     **/
    @Path("coordinatorGroupMember")
    public Response coordinatorGroupMember(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        if(NullUtil.isEmpty(subscribe.getApp())||NullUtil.isEmpty(subscribe.getTopic())){ return  Responses.error(ErrorCode.BadRequest.getCode(),"app 和 topic 不能为空");}
        return Responses.success(coordinatorMonitorService.findCoordinatorGroupMember(subscribe));
    }

    /**
     * 指定 app 的协调者信息
     * @param subscribe
     **/
    @Path("coordinatorGroupExpiredMember")
    public Response coordinatorGroupExpiredMember(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        if(NullUtil.isEmpty(subscribe.getApp())||NullUtil.isEmpty(subscribe.getTopic())){ return  Responses.error(ErrorCode.BadRequest.getCode(),"app 和 topic 不能为空");}
        return Responses.success(coordinatorMonitorService.findExpiredCoordinatorGroupMember(subscribe));
    }

    @Override
    public void clean() {
        brokerMonitorService = null;
        brokerMessageService = null;
        coordinatorMonitorService = null;
    }
}
