package com.jd.journalq.nsr;

import com.jd.journalq.model.domain.Consumer;
import com.jd.journalq.model.query.QConsumer;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface ConsumerNameServerService extends NsrService<Consumer,QConsumer,String> {
    List<Consumer> syncConsumer(byte clientType) throws Exception;

    List<String> findAllSubscribeGroups() throws Exception;

}
