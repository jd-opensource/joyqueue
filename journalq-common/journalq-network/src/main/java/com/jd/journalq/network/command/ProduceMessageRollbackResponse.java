package com.jd.journalq.network.command;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * ProduceMessageRollbackResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageRollbackResponse extends JMQPayload {

    private JMQCode code;

    public ProduceMessageRollbackResponse() {

    }

    public ProduceMessageRollbackResponse(JMQCode code) {
        this.code = code;
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_ROLLBACK_ACK.getCode();
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }
}