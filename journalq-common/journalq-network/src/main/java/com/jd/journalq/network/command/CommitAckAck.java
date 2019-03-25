package com.jd.journalq.network.command;

import com.google.common.collect.Table;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * CommitAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckAck extends JMQPayload {

    private Table<String, Short, JMQCode> result;

    @Override
    public int type() {
        return JMQCommandType.COMMIT_ACK_ACK.getCode();
    }

    public void setResult(Table<String, Short, JMQCode> result) {
        this.result = result;
    }

    public Table<String, Short, JMQCode> getResult() {
        return result;
    }
}