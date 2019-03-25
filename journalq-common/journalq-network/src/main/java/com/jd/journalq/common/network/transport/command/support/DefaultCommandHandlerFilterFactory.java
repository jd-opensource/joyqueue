package com.jd.journalq.common.network.transport.command.support;

import com.google.common.collect.Lists;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerFilter;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.Collections;
import java.util.List;

/**
 * 默认命令调用链工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public class DefaultCommandHandlerFilterFactory implements CommandHandlerFilterFactory {

    private List<CommandHandlerFilter> commandHandlerFilters;

    public DefaultCommandHandlerFilterFactory() {
        this.commandHandlerFilters = initCommandHandlerFilters();
    }

    @Override
    public List<CommandHandlerFilter> getFilters() {
        return Collections.unmodifiableList(commandHandlerFilters);
    }

    protected List<CommandHandlerFilter> initCommandHandlerFilters() {
        List<CommandHandlerFilter> commandHandlerFilters = getCommandHandlerFilters();
        commandHandlerFilters.sort(new CommandHandlerFilterComparator());
        return commandHandlerFilters;
    }

    protected List<CommandHandlerFilter> getCommandHandlerFilters() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(CommandHandlerFilter.class));
    }
}