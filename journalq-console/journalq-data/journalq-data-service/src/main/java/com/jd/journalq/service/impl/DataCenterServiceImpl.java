package com.jd.journalq.service.impl;

import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.model.domain.DataCenter;
import com.jd.journalq.model.query.QDataCenter;
import com.jd.journalq.nsr.model.DataCenterQuery;
import com.jd.journalq.service.DataCenterService;
import com.jd.journalq.nsr.DataCenterNameServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/27.
 */
@Service("dataCenterService")
public class DataCenterServiceImpl implements DataCenterService {
    private static final Logger logger = LoggerFactory.getLogger(DataCenterServiceImpl.class);
    @Autowired
    private DataCenterNameServerService dataCenterNameServerService;

    @Override
    public List<DataCenter> findAllDataCenter() throws Exception {
        return   dataCenterNameServerService.findAllDataCenter(null);
    }

    @Override
    public DataCenter findById(String s) throws Exception {
        return dataCenterNameServerService.findById(s);
    }

    @Override
    public PageResult<DataCenter> findByQuery(QPageQuery<QDataCenter> query) {
        try {
            return dataCenterNameServerService.findByQuery(query);
        } catch (Exception e) {
            logger.error("findByQuery exception",e);
            throw new RuntimeException("",e);
        }
    }

    @Override
    public List<DataCenter> findByQuery(QDataCenter query) {
        DataCenterQuery dataCenterQuery = new DataCenterQuery();
        if (query != null) {
            dataCenterQuery.setRegion(query.getRegion());
            dataCenterQuery.setCode(query.getCode());
        }
        try {
            return dataCenterNameServerService.findAllDataCenter(dataCenterQuery);
        } catch (Exception e) {
            logger.error("findByQuery exception",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int add(DataCenter model) {
        try {
            return dataCenterNameServerService.add(model);
        } catch (Exception e) {
            logger.error("add exception",e);
        }
        return 0;
    }

    @Override
    public int delete(DataCenter model) {
        try {
            return dataCenterNameServerService.delete(model);
        } catch (Exception e) {
            logger.error("delete exception",e);
        }
        return 0;
    }

    @Override
    public int update(DataCenter model) {
        try {
           return dataCenterNameServerService.update(model);
        } catch (Exception e) {
            logger.error("update exception",e);
        }
        return 0;
    }

}
