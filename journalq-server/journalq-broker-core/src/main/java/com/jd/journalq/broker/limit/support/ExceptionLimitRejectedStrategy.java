package com.jd.journalq.broker.limit.support;

import com.jd.journalq.broker.limit.LimitRejectedStrategy;
import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.broker.limit.exception.LimitRejectedException;
import com.jd.journalq.network.transport.command.Command;

/**
 * ExceptionLimitRejectedStrategy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class ExceptionLimitRejectedStrategy implements LimitRejectedStrategy {

    @Override
    public Command execute(LimitContext context) {
        throw new LimitRejectedException(context.getRequest(), context.getResponse());
    }

    @Override
    public String type() {
        return "exception";
    }
}