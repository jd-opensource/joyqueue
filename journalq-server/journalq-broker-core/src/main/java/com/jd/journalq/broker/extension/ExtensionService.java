package com.jd.journalq.broker.extension;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.toolkit.lang.LifeCycle;

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