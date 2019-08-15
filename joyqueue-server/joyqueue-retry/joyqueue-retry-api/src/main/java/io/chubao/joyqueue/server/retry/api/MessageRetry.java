package io.chubao.joyqueue.server.retry.api;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;

/**
 * 消息重试接口
 * <p>
 * Created by chengzhiliang on 2018/9/13.
 */
public interface MessageRetry<T> extends LifeCycle, PropertySupplierAware {

    /**
     * 增加重试
     *
     * @param retryMessageModelList 重试实例集合
     * @throws JoyQueueException
     */
    void addRetry(List<RetryMessageModel> retryMessageModelList) throws JoyQueueException;

    /**
     * 更新重试消息状态到重试成功
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JoyQueueException 操作失败时
     */
    void retrySuccess(String topic, String app, T[] messageIds) throws JoyQueueException;

    /**
     * 更新重试消息状态到重试错误
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JoyQueueException 操作失败时
     */
    void retryError(String topic, String app, T[] messageIds) throws JoyQueueException;

    /**
     * 更新重试消息状态为重试过期
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JoyQueueException 操作失败时
     */
    void retryExpire(String topic, String app, T[] messageIds) throws JoyQueueException;

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
    List<RetryMessageModel> getRetry(String topic, String app, short count, long startIndex) throws JoyQueueException;

    /**
     * 获取重试数据量
     *
     * @param topic 主题
     * @param app   应用
     * @return 重试数据量
     * @throws JoyQueueException
     */
    int countRetry(String topic, String app) throws JoyQueueException;

    /**
     * 设置重试策略和主题发现
     *
     * @param retryPolicyProvider
     */
    void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider);

}