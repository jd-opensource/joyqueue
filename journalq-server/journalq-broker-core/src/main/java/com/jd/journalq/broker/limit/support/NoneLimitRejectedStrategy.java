package com.jd.journalq.broker.limit.support;

import com.jd.journalq.broker.limit.LimitRejectedStrategy;
import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.network.transport.command.Command;

/**
 * NoneLimitRejectedStrategy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class NoneLimitRejectedStrategy implements LimitRejectedStrategy {

    @Override
    public Command execute(LimitContext context) {
        return context.getResponse();
    }

    @Override
    public String type() {
        return "none";
    }
}