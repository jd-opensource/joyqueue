package com.jd.journalq.server.archive.store.api;

import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.server.archive.store.model.AchivePosition;
import com.jd.journalq.server.archive.store.model.ConsumeLog;
import com.jd.journalq.server.archive.store.model.Query;
import com.jd.journalq.server.archive.store.model.SendLog;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;

/**
 * 归档接口
 * <br>
 * 持久化消费信息、生产信息、归档进度信息
 * <p>
 * Created by chengzhiliang on 2018/12/4.
 */
public interface ArchiveStore extends LifeCycle {

    /**
     * 持久化消费日志
     *
     * @param consumeLogs
     * @throws JMQException
     */
    void putConsumeLog(List<ConsumeLog> consumeLogs) throws JMQException;

    /**
     * 持久化发送日志
     *
     * @param sendLogs
     * @throws JMQException
     */
    void putSendLog(List<SendLog> sendLogs) throws JMQException;

    /**
     * 持久化归档进度信息
     *
     * @param achivePosition
     * @throws JMQException
     */
    void putPosition(AchivePosition achivePosition) throws JMQException;

    /**
     * 查询归档进度信息
     *
     * @param topic
     * @param partition
     * @return
     * @throws JMQException
     */
    Long getPosition(String topic, short partition) throws JMQException;

    /**
     * 查看发送日志
     *
     * @param query
     * @return
     * @throws JMQException
     */
    List<SendLog> scanSendLog(Query query) throws JMQException;

    /**
     * 查看一条发送日志
     *
     * @param query
     * @return
     * @throws JMQException
     */
    SendLog getOneSendLog(Query query) throws JMQException;

    /**
     * 查询消费日志
     *
     * @param messageId
     * @param count
     * @return
     * @throws JMQException
     */
    List<ConsumeLog> scanConsumeLog(String messageId,Integer count) throws JMQException;
}
