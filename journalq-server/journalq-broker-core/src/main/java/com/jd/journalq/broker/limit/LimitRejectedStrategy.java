package com.jd.journalq.broker.limit;

import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.network.transport.command.Command;
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