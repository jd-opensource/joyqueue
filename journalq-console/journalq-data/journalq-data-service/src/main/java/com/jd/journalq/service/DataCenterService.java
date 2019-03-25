package com.jd.journalq.service;

import com.jd.journalq.model.domain.DataCenter;
import com.jd.journalq.model.query.QDataCenter;
import com.jd.journalq.nsr.NsrService;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/27.
 */
public interface DataCenterService extends NsrService<DataCenter,QDataCenter,String> {

    List<DataCenter> findAllDataCenter() throws Exception;
}
