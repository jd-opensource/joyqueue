package io.chubao.joyqueue.broker.limit;

import io.chubao.joyqueue.broker.limit.domain.LimitContext;
import io.chubao.joyqueue.network.transport.command.Command;
import com.jd.laf.extension.Type;

/**
 * LimitRejectedStrategy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public interface LimitRejectedStrategy extends Type<String> {

    Command execute(LimitContext context);
}