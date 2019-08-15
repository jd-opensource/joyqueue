package io.chubao.joyqueue.service;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.model.query.QArchive;
import io.chubao.joyqueue.server.archive.store.api.ArchiveStore;
import io.chubao.joyqueue.server.archive.store.model.ConsumeLog;
import io.chubao.joyqueue.server.archive.store.model.SendLog;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/7.
 */
public interface ArchiveService {

    void register(ArchiveStore archiveStore);

    /**
     * 分页查询归档
     * @param qArchive
     * @return
     */
    List<SendLog> findByQuery(QArchive qArchive) throws JoyQueueException;

    /**
     * 按id查询归档
     * @param messageId
     * @return
     */
    SendLog findSendLog(String topic, Long time, String businessId, String messageId) throws JoyQueueException;

    /**
     * 获取消费详情
     * @param messageId
     * @return
     * @throws JoyQueueException
     */
    List<ConsumeLog> findConsumeLog(String messageId, Integer count) throws JoyQueueException;

    /**
     * 归档服务是否可用
     * @throws Exception
     */
    boolean isServerEnabled();
}
