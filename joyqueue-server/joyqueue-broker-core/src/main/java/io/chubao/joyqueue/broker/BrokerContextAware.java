package io.chubao.joyqueue.broker;

/**
 * broker上下文注入
 *
 * author: gaohaoxiang
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