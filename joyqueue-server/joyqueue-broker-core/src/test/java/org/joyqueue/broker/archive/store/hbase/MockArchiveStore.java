package org.joyqueue.broker.archive.store.hbase;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.server.archive.store.api.ArchiveStore;
import org.joyqueue.server.archive.store.model.AchivePosition;
import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.server.archive.store.model.Query;
import org.joyqueue.server.archive.store.model.SendLog;

import java.util.List;

public class MockArchiveStore implements ArchiveStore {

    int exception = 0;
    int exceptionCount = 3;

    @Override
    public void putConsumeLog(List<ConsumeLog> consumeLogs, PointTracer tracer) throws JoyQueueException {
        if (++exception % 3 == 0) {
            throw new JoyQueueException("Exception for store", JoyQueueCode.SE_IO_ERROR.getCode());
        }
    }

    @Override
    public void putSendLog(List<SendLog> sendLogs, PointTracer tracer) throws JoyQueueException {

    }

    @Override
    public void putPosition(AchivePosition achivePosition) throws JoyQueueException {

    }

    @Override
    public Long getPosition(String topic, short partition) throws JoyQueueException {
        return null;
    }

    @Override
    public void cleanPosition(String topic, short partition) throws JoyQueueException {

    }

    @Override
    public List<SendLog> scanSendLog(Query query) throws JoyQueueException {
        return null;
    }

    @Override
    public SendLog getOneSendLog(Query query) throws JoyQueueException {
        return null;
    }

    @Override
    public List<ConsumeLog> scanConsumeLog(String messageId, Integer count) throws JoyQueueException {
        return null;
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
        return false;
    }
}
