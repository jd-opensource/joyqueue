package io.chubao.joyqueue.server.archive.store.api;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.server.archive.store.model.AchivePosition;
import io.chubao.joyqueue.server.archive.store.model.ConsumeLog;
import io.chubao.joyqueue.server.archive.store.model.Query;
import io.chubao.joyqueue.server.archive.store.model.SendLog;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

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
     * @throws JoyQueueException
     */
    void putConsumeLog(List<ConsumeLog> consumeLogs) throws JoyQueueException;

    /**
     * 持久化发送日志
     *
     * @param sendLogs
     * @throws JoyQueueException
     */
    void putSendLog(List<SendLog> sendLogs) throws JoyQueueException;

    /**
     * 持久化归档进度信息
     *
     * @param achivePosition
     * @throws JoyQueueException
     */
    void putPosition(AchivePosition achivePosition) throws JoyQueueException;

    /**
     * 查询归档进度信息
     *
     * @param topic
     * @param partition
     * @return
     * @throws JoyQueueException
     */
    Long getPosition(String topic, short partition) throws JoyQueueException;

    /**
     * 查看发送日志
     *
     * @param query
     * @return
     * @throws JoyQueueException
     */
    List<SendLog> scanSendLog(Query query) throws JoyQueueException;

    /**
     * 查看一条发送日志
     *
     * @param query
     * @return
     * @throws JoyQueueException
     */
    SendLog getOneSendLog(Query query) throws JoyQueueException;

    /**
     * 查询消费日志
     *
     * @param messageId
     * @param count
     * @return
     * @throws JoyQueueException
     */
    List<ConsumeLog> scanConsumeLog(String messageId,Integer count) throws JoyQueueException;
}
