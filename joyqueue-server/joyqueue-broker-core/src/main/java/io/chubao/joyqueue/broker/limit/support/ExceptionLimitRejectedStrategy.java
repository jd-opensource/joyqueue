package io.chubao.joyqueue.broker.limit.support;

import io.chubao.joyqueue.broker.limit.LimitRejectedStrategy;
import io.chubao.joyqueue.broker.limit.domain.LimitContext;
import io.chubao.joyqueue.broker.limit.exception.LimitRejectedException;
import io.chubao.joyqueue.network.transport.command.Command;

/**
 * ExceptionLimitRejectedStrategy
 *
 * author: gaohaoxiang
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