package io.chubao.joyqueue.broker.limit.exception;

import io.chubao.joyqueue.network.transport.command.Command;

/**
 * LimitRejectedException
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class LimitRejectedException extends LimitException {

    private Command request;
    private Command response;

    public LimitRejectedException() {
    }

    public LimitRejectedException(Command request, Command response) {
        this.request = request;
        this.response = response;
    }

    public LimitRejectedException(String message) {
        super(message);
    }

    public LimitRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LimitRejectedException(Throwable cause) {
        super(cause);
    }

    public LimitRejectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}