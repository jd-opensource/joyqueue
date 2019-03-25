package com.jd.journalq.nsr;

import com.jd.journalq.model.domain.DataCenter;
import com.jd.journalq.model.query.QDataCenter;
import com.jd.journalq.nsr.model.DataCenterQuery;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface DataCenterNameServerService extends NsrService<DataCenter,QDataCenter,String> {
    List<DataCenter> findAllDataCenter(DataCenterQuery dataCenterQuery) throws Exception;
}
