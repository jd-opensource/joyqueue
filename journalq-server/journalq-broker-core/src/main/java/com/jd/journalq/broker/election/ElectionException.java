package com.jd.journalq.broker.election;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ElectionException extends JMQException {
    public ElectionException(String message) {
        super(message, JMQCode.FW_ELECTION_ERROR.getCode());
    }

    public ElectionException(String message, Throwable cause) {
        super(message, cause, JMQCode.FW_ELECTION_ERROR.getCode());
    }
}
