package com.jd.journalq.service.impl;

import com.jd.journalq.model.query.QArchive;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.server.archive.store.QueryCondition;
import com.jd.journalq.server.archive.store.api.ArchiveStore;
import com.jd.journalq.server.archive.store.model.ConsumeLog;
import com.jd.journalq.server.archive.store.model.SendLog;
import com.jd.journalq.service.ArchiveService;
import com.jd.journalq.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/7.
 */
@Service("archiveService")
public class ArchiveServiceImpl implements ArchiveService {
    private Logger logger = LoggerFactory.getLogger(ArchiveServiceImpl.class);
    @Autowired(required = false)
    private ArchiveStore archiveStore;

    @Override
    public void register(ArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    @Override
    public List<SendLog> findByQuery(QArchive qPageQuery) throws JMQException {
        QueryCondition queryCondition = conditionConvert(qPageQuery);
        List<SendLog> sendLogs = archiveStore.scanSendLog(queryCondition);
        return sendLogs;
    }

    @Override
    public SendLog findSendLog(String topic,Long time,String businessId,String messageId) throws JMQException {
        QueryCondition queryCondition = new QueryCondition();
        QueryCondition.RowKey startRow = new QueryCondition.RowKey();
        startRow.setBusinessId(businessId);
        startRow.setMessageId(messageId);
        startRow.setTime(time);
        startRow.setTopic(topic);
        queryCondition.setRowKey(startRow);
        SendLog sendLog = archiveStore.getOneSendLog(queryCondition);
        return sendLog;
    }

    @Override
    public List<ConsumeLog> findConsumeLog(String messageId, Integer count) throws JMQException {
        return archiveStore.scanConsumeLog(messageId,count);
    }



    /**
     * query转QueryCondition
     * @param qArchive
     * @return
     */
    private QueryCondition conditionConvert(QArchive qArchive) {
        QueryCondition queryCondition = new QueryCondition();

        //设置起始row
        QueryCondition.RowKey startRow = new QueryCondition.RowKey();
        startRow.setTopic(qArchive.getTopic());
        if (NullUtil.isNotBlank(qArchive.getBusinessId())) {
            startRow.setBusinessId(qArchive.getBusinessId());
        }
        if (NullUtil.isNotBlank(qArchive.getMessageId())) {
            startRow.setMessageId(qArchive.getMessageId());
        }
        if (qArchive.getBeginTime() != null) {
            startRow.setTime(qArchive.getBeginTime().getTime());
        }
        if (qArchive.getSendTime() != null) {
            startRow.setTime(qArchive.getSendTime().getTime());
        }
        queryCondition.setStartRowKey(startRow);

        //设置结束row
        QueryCondition.RowKey endRow = new QueryCondition.RowKey();
        endRow.setTopic(qArchive.getTopic());

        if (NullUtil.isNotBlank(qArchive.getBusinessId())) {
            endRow.setBusinessId(qArchive.getBusinessId());
        }
        if (NullUtil.isNotBlank(qArchive.getMessageId())) {
            endRow.setMessageId(qArchive.getMessageId());
        }
        if (qArchive.getEndTime() != null) {
            endRow.setTime(qArchive.getEndTime().getTime());
        }
        queryCondition.setStopRowKey(endRow);
        //设置数量
        queryCondition.setCount(qArchive.getCount());
        return queryCondition;
    }

}
