package com.jd.journalq.common.network.transport.command.handler.filter;

import java.util.List;

/**
 * 命令处理调用链工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public interface CommandHandlerFilterFactory {

    List<CommandHandlerFilter> getFilters();
}