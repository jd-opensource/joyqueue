package io.chubao.joyqueue.broker.limit.support;

import io.chubao.joyqueue.broker.limit.LimitRejectedStrategy;
import io.chubao.joyqueue.broker.limit.domain.LimitContext;
import io.chubao.joyqueue.network.transport.command.Command;

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
            Thread.currentThread().sleep(context.getDelay());
        } catch (InterruptedException e) {
        }
        return context.getResponse();
    }

    @Override
    public String type() {
        return "block";
    }
}