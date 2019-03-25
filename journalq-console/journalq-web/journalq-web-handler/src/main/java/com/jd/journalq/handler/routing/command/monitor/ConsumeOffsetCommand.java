package com.jd.journalq.handler.routing.command.monitor;

import com.jd.journalq.monitor.PartitionAckMonitorInfo;
import com.jd.journalq.handler.binder.annotation.Body;
import com.jd.journalq.handler.binder.annotation.GenericValue;
import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.service.ConsumeOffsetService;
import com.jd.journalq.handler.binder.BodyType;
import com.jd.journalq.model.domain.PartitionOffset;
import com.jd.journalq.model.domain.ResetOffsetInfo;
import com.jd.journalq.model.domain.Subscribe;
import com.jd.journalq.util.NullUtil;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import java.util.ArrayList;
import java.util.List;

public class ConsumeOffsetCommand implements Command<Response>, Poolable {


    @GenericValue
    private ConsumeOffsetService consumeOffsetService;

    @Override
    public Response execute() throws Exception {
        throw  new UnsupportedOperationException("unsupported");
    }

    /**
     *
     *
     */
    @Path("offsets")
    public Response offsets(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe){
        return Responses.success(consumeOffsetService.offsets(subscribe));
    }

    /**
     *
     *
     */
    @Path("resetBound")
    public Response offsetBound(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe, @ParamterValue("location") String location){
        PartitionOffset.Location loc=PartitionOffset.Location.valueOf(location);
        List<PartitionOffset> partitionOffsets=new ArrayList<>();
        List<PartitionAckMonitorInfo>  partitionAckMonitorInfos=consumeOffsetService.offsets(subscribe);
        PartitionOffset partitionOffset;
        for(PartitionAckMonitorInfo p:partitionAckMonitorInfos){
             partitionOffset=new PartitionOffset();
             partitionOffset.setPartition(p.getPartition());
             if(loc== PartitionOffset.Location.MAX){
                 partitionOffset.setOffset(p.getRightIndex());
             }else partitionOffset.setOffset(p.getLeftIndex());
             partitionOffsets.add(partitionOffset);
        }
        boolean result=consumeOffsetService.resetOffset(subscribe, partitionOffsets);
        return result?Responses.success("success"):Responses.error(ErrorCode.ServiceError.getCode(),"reset failed");
    }

    /**
     *
     *
     */
    @Path("resetByTime")
    public Response resetByTime(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe, @ParamterValue("timestamp") String timestamp){
        Long  time=Long.valueOf(timestamp);
        boolean result=consumeOffsetService.resetOffset(subscribe,time);
        return result?Responses.success("success"):Responses.error(ErrorCode.ServiceError.getCode(),"reset failed");
    }

    /**
     * Reset partition offset for @code subscribe
     *
     */
    @Path("resetPartition")
    public Response resetPartition(@Body(typeindex = 0,type = BodyType.JSON) Subscribe subscribe, @ParamterValue("partition") String partition, @ParamterValue("offset") String offset){
        if(NullUtil.isEmpty(partition)||NullUtil.isEmpty(offset)) {
            return  Responses.error(ErrorCode.BadRequest.getCode(),"partition and offset can't be null");
        }
        boolean result=consumeOffsetService.resetOffset(subscribe,Short.valueOf(partition),Long.valueOf(offset));
        return result?Responses.success("success"):Responses.error(ErrorCode.ServiceError.getCode(),"reset failed");

    }


    /**
     *
     * Reset  offsets for @code subscribe
     *
     */
    @Path("reset")
    public Response resetOffsets(@Body(typeindex = 0,type = BodyType.JSON) ResetOffsetInfo offsetInfo){
        boolean result=consumeOffsetService.resetOffset(offsetInfo.getSubscribe(),offsetInfo.getPartitionOffsets());
        return result?Responses.success("success"):Responses.error(ErrorCode.ServiceError.getCode(),"reset failed");
    }


    @Override
    public void clean() {

    }
}
