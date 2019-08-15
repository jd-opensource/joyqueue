package io.chubao.joyqueue.network.transport.command.support;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.transport.command.handler.filter.CommandHandlerFilter;
import io.chubao.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * DefaultCommandHandlerFilterFactory
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
        return commandHandlerFilters;
    }

    protected List<CommandHandlerFilter> initCommandHandlerFilters() {
        List<CommandHandlerFilter> commandHandlerFilters = getCommandHandlerFilters();
        commandHandlerFilters.sort(new CommandHandlerFilterComparator());
        return commandHandlerFilters;
    }

    protected List getCommandHandlerFilters() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(CommandHandlerFilter.class));
    }
}