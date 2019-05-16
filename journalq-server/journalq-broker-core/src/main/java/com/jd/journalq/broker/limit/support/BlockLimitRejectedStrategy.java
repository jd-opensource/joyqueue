package com.jd.journalq.broker.limit.support;

import com.jd.journalq.broker.limit.LimitRejectedStrategy;
import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.network.transport.command.Command;

/**
 * BlockLimitRejectedStrategy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class BlockLimitRejectedStrategy implements LimitRejectedStrategy {

    @Override
    public Command execute(LimitContext context) {
        try {
            Thread.currentThread().wait(context.getDelay());
        } catch (InterruptedException e) {
        }
        return context.getResponse();
    }

    @Override
    public String type() {
        return "block";
    }
}