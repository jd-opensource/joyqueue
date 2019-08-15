package io.chubao.joyqueue.broker.election;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.exception.JoyQueueException;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ElectionException extends JoyQueueException {
    public ElectionException(String message) {
        super(message, JoyQueueCode.FW_ELECTION_ERROR.getCode());
    }

    public ElectionException(String message, Throwable cause) {
        super(message, cause, JoyQueueCode.FW_ELECTION_ERROR.getCode());
    }
}
