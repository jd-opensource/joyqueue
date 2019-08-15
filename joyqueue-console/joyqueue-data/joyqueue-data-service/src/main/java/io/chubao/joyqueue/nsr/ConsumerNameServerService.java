package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.domain.Consumer;
import io.chubao.joyqueue.model.query.QConsumer;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface ConsumerNameServerService extends NsrService<Consumer,QConsumer,String> {
    List<Consumer> syncConsumer(byte clientType) throws Exception;

    List<String> findAllSubscribeGroups() throws Exception;

}
