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
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.model.domain.PartitionOffset;
import org.joyqueue.model.domain.ResetOffsetInfo;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.monitor.PartitionLeaderAckMonitorInfo;
import org.joyqueue.service.ConsumeOffsetService;
import org.joyqueue.service.OperLogService;
import org.joyqueue.util.LocalSession;
import org.joyqueue.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConsumeOffsetCommand implements Command<Response>, Poolable {

    private static final Logger logger = LoggerFactory.getLogger(ConsumeOffsetCommand.class);

    @Value(nullable = false)
    private ConsumeOffsetService consumeOffsetService;

    @Value(nullable = false)
    private OperLogService operLogService;

    @Override
    public Response execute() throws Exception {
        throw new UnsupportedOperationException("unsupported");
    }

    @Path("offsets")
    public Response offsets(@Body Subscribe subscribe) {
        try {
            return Responses.success(consumeOffsetService.offsets(subscribe));
        } catch (Exception e) {
            logger.error("query consumer offset info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
    }

    private void logOperation(String topic, String app, String operation)  {
        OperLog log = new OperLog();
        log.setIdentity(topic);
        Long id = LocalSession.getSession().getUser().getId();
        String code = LocalSession.getSession().getUser().getCode();
        log.setCreateBy(new Identity(id, code));
        log.setCreateTime(new Date());
        log.setUpdateBy(new Identity(id, code));
        log.setUpdateTime(new Date());
        log.setType(OperLog.Type.CONSUMER.value());
        log.setOperType(OperLog.OperType.UPDATE.value());
        log.setTarget("topic: " + topic + " app: " + app + " operation: " + operation);
        operLogService.add(log);
    }

    /**
     * offset
     */
    @Path("resetBound")
    public Response offsetBound(@Body Subscribe subscribe, @QueryParam("location") String location) {
        PartitionOffset.Location loc = PartitionOffset.Location.valueOf(location);
        List<PartitionOffset> partitionOffsets = new ArrayList<>();
        List<PartitionLeaderAckMonitorInfo> partitionAckMonitorInfos = consumeOffsetService.offsets(subscribe);
        PartitionOffset partitionOffset;
        for (PartitionLeaderAckMonitorInfo p : partitionAckMonitorInfos) {
            if (p.isLeader()) {
                partitionOffset = new PartitionOffset();
                partitionOffset.setPartition(p.getPartition());
                if (loc == PartitionOffset.Location.MAX) {
                    partitionOffset.setOffset(p.getRightIndex());
                } else partitionOffset.setOffset(p.getLeftIndex());
                partitionOffsets.add(partitionOffset);
                logOperation(subscribe.getTopic().getCode(), subscribe.getApp().getCode(), "resetBound: " + partitionOffset);
            }
        }
        boolean result = consumeOffsetService.resetOffset(subscribe, partitionOffsets);
        return result ? Responses.success("success") : Responses.error(ErrorCode.ServiceError.getCode(), "reset failed");
    }

    /**
     * Reset partition offset by timestamp
     */
    @Path("resetByTime")
    public Response resetByTime(@Body Subscribe subscribe, @QueryParam("timestamp") String timestamp) {
        try {
            Long time = Long.valueOf(timestamp);
            logOperation(subscribe.getTopic().getCode(), subscribe.getApp().getCode(), "resetByTime: " + timestamp);
            boolean result = consumeOffsetService.resetOffset(subscribe, time);
            return result ? Responses.success("success") : Responses.error(ErrorCode.ServiceError.getCode(), "reset failed");
        } catch (Exception e) {
            logger.error("query consumer offset info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
    }

    /**
     * Reset partition offset for @code subscribe
     */
    @Path("resetPartition")
    public Response resetPartition(@Body Subscribe subscribe, @QueryParam("partition") String partition, @QueryParam("offset") String offset) {
        try {
            if (NullUtil.isEmpty(partition) || NullUtil.isEmpty(offset)) {
                return Responses.error(ErrorCode.BadRequest.getCode(), "partition and offset can't be null");
            }
            logOperation(subscribe.getTopic().getCode(), subscribe.getApp().getCode(), "resetPartition: partition=" + partition + ", offset=" + offset);
            boolean result = consumeOffsetService.resetOffset(subscribe, Short.valueOf(partition), Long.valueOf(offset));
            return result ? Responses.success("success") : Responses.error(ErrorCode.ServiceError.getCode(), "reset failed");
        } catch (Exception e) {
            logger.error("query consumer offset info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }

    }


    /**
     * Reset  offsets for @code subscribe
     */
    @Path("reset")
    public Response resetOffsets(@Body ResetOffsetInfo offsetInfo) {
        try {
            logOperation(offsetInfo.getSubscribe().getTopic().getCode(), offsetInfo.getSubscribe().getApp().getCode(), "resetOffsets: " + offsetInfo.getPartitionOffsets());
            boolean result = consumeOffsetService.resetOffset(offsetInfo.getSubscribe(), offsetInfo.getPartitionOffsets());
            return result ? Responses.success("success") : Responses.error(ErrorCode.ServiceError.getCode(), "reset failed");
        } catch (Exception e) {
            logger.error("query consumer offset info error.", e);
            return Responses.error(ErrorCode.NoTipError.getCode(), ErrorCode.NoTipError.getStatus(), e.getMessage());
        }
    }


    @Override
    public void clean() {

    }
}
