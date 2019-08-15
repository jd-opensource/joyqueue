package io.chubao.joyqueue.broker;

/**
 * broker上下文注入
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public interface BrokerContextAware {
    /**
     * set broker context
     *
     * @param brokerContext
     */
    void setBrokerContext(BrokerContext brokerContext);
}