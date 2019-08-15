package io.chubao.joyqueue.broker.limit.support;

import io.chubao.joyqueue.broker.limit.LimitRejectedStrategy;
import io.chubao.joyqueue.broker.limit.domain.LimitContext;
import io.chubao.joyqueue.network.transport.command.Command;

/**
 * NoneLimitRejectedStrategy
 *
 * author: gaohaoxiang
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