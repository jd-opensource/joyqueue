package com.jd.journalq.broker.consumer.filter;

import com.jd.journalq.common.exception.JMQException;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 消息过滤接口
 * <p>
 * Created by chengzhiliang on 2019/2/20.
 */
public interface MessageFilter {

    /**
     * 消息过滤
     *
     * @param byteBufferList 消息字节缓存集合
     * @param filterCallback 消息过滤后的回调
     * @return
     * @throws JMQException
     */
    List<ByteBuffer> filter(List<ByteBuffer> byteBufferList, FilterCallback filterCallback) throws JMQException;

    /**
     * 设置过滤规则
     *
     * @param rule 过滤规则
     */
    void setRule(String rule);

}
