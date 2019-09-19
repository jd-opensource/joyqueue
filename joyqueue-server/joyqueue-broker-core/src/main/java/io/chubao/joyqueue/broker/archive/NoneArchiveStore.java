package io.chubao.joyqueue.broker.archive;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.server.archive.store.api.ArchiveStore;
import io.chubao.joyqueue.server.archive.store.model.AchivePosition;
import io.chubao.joyqueue.server.archive.store.model.ConsumeLog;
import io.chubao.joyqueue.server.archive.store.model.Query;
import io.chubao.joyqueue.server.archive.store.model.SendLog;

import java.util.Collections;
import java.util.List;

/**
 * NoneArchiveStore
 * author: gaohaoxiang
 * date: 2019/9/9
 */
public class NoneArchiveStore implements ArchiveStore {

    @Override
    public void putConsumeLog(List<ConsumeLog> consumeLogs) throws JoyQueueException {

    }

    @Override
    public void putSendLog(List<SendLog> sendLogs) throws JoyQueueException {

    }

    @Override
    public void putPosition(AchivePosition achivePosition) throws JoyQueueException {

    }

    @Override
    public Long getPosition(String topic, short partition) throws JoyQueueException {
        return 0L;
    }

    @Override
    public List<SendLog> scanSendLog(Query query) throws JoyQueueException {
        return Collections.emptyList();
    }

    @Override
    public SendLog getOneSendLog(Query query) throws JoyQueueException {
        return new SendLog();
    }

    @Override
    public List<ConsumeLog> scanConsumeLog(String messageId, Integer count) throws JoyQueueException {
        return Collections.emptyList();
    }

    @Override
    public void setNameSpace(String nameSpace) {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }
}