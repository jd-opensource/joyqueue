package io.chubao.joyqueue.broker.extension;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

/**
 * ExtensionService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
@Deprecated
public interface ExtensionService extends LifeCycle {

    void init(BrokerContext brokerContext);
}