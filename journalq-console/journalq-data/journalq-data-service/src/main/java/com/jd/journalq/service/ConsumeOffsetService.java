package com.jd.journalq.service;

import com.jd.journalq.monitor.PartitionAckMonitorInfo;
import com.jd.journalq.model.domain.PartitionOffset;
import com.jd.journalq.model.domain.Subscribe;

import java.util.List;

//todo 待移走
public interface ConsumeOffsetService {

    /**
     *
     * @return all partition low ,upper index bound and current ack index
     **/
    List<PartitionAckMonitorInfo> offsets(Subscribe subscribe);


    /**
     *
     * @return  partition  offset
     **/
    long offset(Subscribe subscribe,short partition);

    /**
     * @return  partition message  offset  of @code timeMs
     **/
    List<PartitionAckMonitorInfo> timeOffset(Subscribe subscribe, long timeMs);


    /**
     * Reset partition offset of the @Subscribe
     **/
    boolean resetOffset(Subscribe subscribe,short partition,long offset);

    /**
     *  Reset offset of the @Subscribe by time
     **/
    boolean resetOffset(Subscribe subscribe,long timeMs);


    /**
     *  Reset offset of the @Subscribe by offset
     **/
    boolean resetOffset(Subscribe subscribe, List<PartitionOffset> offsets);

}
