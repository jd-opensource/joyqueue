package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.DataCenter;
import io.chubao.joyqueue.model.query.QDataCenter;
import io.chubao.joyqueue.nsr.NsrService;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/27.
 */
public interface DataCenterService extends NsrService<DataCenter,QDataCenter,String> {

    List<DataCenter> findAllDataCenter() throws Exception;
}
