package com.jd.journalq.server.retry.api;

import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;

/**
 * 消息重试接口
 * <p>
 * Created by chengzhiliang on 2018/9/13.
 */
public interface MessageRetry<T> extends LifeCycle {

    /**
     * 增加重试
     *
     * @param retryMessageModelList 重试实例集合
     * @throws JMQException
     */
    void addRetry(List<RetryMessageModel> retryMessageModelList) throws JMQException;

    /**
     * 更新重试消息状态到重试成功
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JMQException 操作失败时
     */
    void retrySuccess(String topic, String app, T[] messageIds) throws JMQException;

    /**
     * 更新重试消息状态到重试错误
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JMQException 操作失败时
     */
    void retryError(String topic, String app, T[] messageIds) throws JMQException;

    /**
     * 更新重试消息状态为重试过期
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JMQException 操作失败时
     */
    void retryExpire(String topic, String app, T[] messageIds) throws JMQException;

    /**
     * 查询指定主题和个数的重试消息
     * <p/>
     * <p>
     * 该接口要求实现类返回非空List, 即要么返回非空List，要么抛出异常
     * </p>
     *
     * @param topic      主题
     * @param app        应用
     * @param count      条数
     * @param startIndex 起始ID
     */
    List<RetryMessageModel> getRetry(String topic, String app, short count, long startIndex) throws JMQException;

    /**
     * 获取重试数据量
     *
     * @param topic 主题
     * @param app   应用
     * @return 重试数据量
     * @throws JMQException
     */
    int countRetry(String topic, String app) throws JMQException;

    /**
     * 设置重试策略和主题发现
     *
     * @param retryPolicyProvider
     */
    void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider);

}