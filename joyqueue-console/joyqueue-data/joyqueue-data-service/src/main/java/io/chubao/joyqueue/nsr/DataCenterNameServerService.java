package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.domain.DataCenter;
import io.chubao.joyqueue.model.query.QDataCenter;
import io.chubao.joyqueue.nsr.model.DataCenterQuery;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface DataCenterNameServerService extends NsrService<DataCenter,QDataCenter,String> {
    List<DataCenter> findAllDataCenter(DataCenterQuery dataCenterQuery) throws Exception;
}
