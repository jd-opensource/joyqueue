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
package com.jd.joyqueue.handler.routing.command.monitor;

import com.jd.joyqueue.domain.Broker;
import com.jd.joyqueue.handler.annotation.PageQuery;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.model.domain.BrokerClient;
import com.jd.joyqueue.model.domain.BrokerMonitorRecord;
import com.jd.joyqueue.model.domain.BrokerTopicMonitor;
import com.jd.joyqueue.model.domain.ConnectionMonitorInfoWithIp;
import com.jd.joyqueue.model.domain.SimplifiedBrokeMessage;
import com.jd.joyqueue.model.domain.Subscribe;
import com.jd.joyqueue.monitor.BrokerMessageInfo;
import com.jd.joyqueue.monitor.BrokerMonitorInfo;
import com.jd.joyqueue.monitor.BrokerStartupInfo;
import com.jd.joyqueue.monitor.Client;
import com.jd.joyqueue.handler.error.ErrorCode;
import com.jd.joyqueue.model.query.QMonitor;
import com.jd.joyqueue.model.query.QPartitionGroupMonitor;
import com.jd.joyqueue.service.BrokerMessageService;
import com.jd.joyqueue.service.BrokerMonitorService;
import com.jd.joyqueue.service.BrokerTopicMonitorService;
import com.jd.joyqueue.service.CoordinatorMonitorService;
import com.jd.joyqueue.util.NullUtil;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.lang3.StringUtils;
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
    public Response find(@Body Subscribe subscribe){
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
    public Response findMonitorOnPartition(@Body Subscribe subscribe) {
        try {
            List<BrokerMonitorRecord> record = brokerMonitorService.findMonitorOnPartition(subscribe);
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定topic、app 在broker 上的连接数
     *
     **/
    @Path("findConnectionOnBroker")
    public Response findConnectionOnBroker(@Body Subscribe subscribe) {
        try {
            List<ConnectionMonitorInfoWithIp> record=brokerMonitorService.findConnectionOnBroker(subscribe);
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * 指定topic、app、broker 生产或者消费 partitionGroup 监控
     *
     **/
    @Path("partitionGroupMonitor")
    public Response partitionGroupMonitor(@PageQuery QPageQuery<QMonitor> qPageQuery){
        try {
            PageResult<BrokerTopicMonitor> pageResult = brokerTopicMonitorService.queryTopicsPartitionMointor(qPageQuery);
            return new Response(pageResult.getResult(), pageResult.getPagination());
        } catch (Exception e) {
        logger.error("query broker monitor info error.", e);
        return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.success(record);
        } catch (Exception e) {
            logger.error("query broker monitor info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
    }
    /**
     * 指定topic、app 生产或者消费Client监控汇总信息
     *
     **/
    @Path("previewMessage")
    public Response previewMessage(@Body Subscribe subscribe){
        int defaultCount=10;
        List<SimplifiedBrokeMessage> messages=brokerMessageService.previewMessage(subscribe,defaultCount);
        return Responses.success(messages);
    }

    /**
     * 指定topic、app 生产或者消费Client监控汇总信息
     *
     **/
    @Path("viewMessage")
    public Response viewMessage(@Body Subscribe subscribe,@QueryParam("partition") String partition, @QueryParam("index")String index,@QueryParam("timestamp")String timestamp){
        int defaultCount=10;
        if (StringUtils.isNotEmpty(timestamp) && StringUtils.isEmpty(index)){
            Long indexByTime = brokerMessageService.getPartitionIndexByTime(subscribe,partition,timestamp);
            index = String.valueOf(indexByTime);
        }
        List<BrokerMessageInfo> messages=brokerMessageService.viewMessage(subscribe,partition,index,defaultCount);
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
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
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * broker监控
     * @param brokerId
     * @return
     */
    @Path("findBrokerDetail")
    public Response findBrokerDetail(@QueryParam("brokerId") Long brokerId){
        BrokerMonitorInfo brokerMonitorInfo = brokerTopicMonitorService.findBrokerMonitor(brokerId);
        return Responses.success(brokerMonitorInfo);
    }
    /**
     * broker启动信息
     * @param brokerId
     * @return
     */
    @Path("startInfo")
    public Response startInfo(@QueryParam("brokerId") Long brokerId) throws Exception {
        BrokerStartupInfo brokerStartupInfo = brokerTopicMonitorService.getStartupInfo(brokerId);
        return Responses.success(brokerStartupInfo);
    }

    @Override
    public void clean() {
        brokerMonitorService = null;
        brokerMessageService = null;
        coordinatorMonitorService = null;
    }
}
