package io.chubao.joyqueue.broker.network.protocol;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.helper.AwareHelper;
import io.chubao.joyqueue.network.transport.command.handler.filter.CommandHandlerFilter;
import io.chubao.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import io.chubao.joyqueue.network.transport.command.support.CommandHandlerFilterComparator;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * ProtocolCommandHandlerFilterFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class ProtocolCommandHandlerFilterFactory implements CommandHandlerFilterFactory {

    private BrokerContext brokerContext;
    private List<CommandHandlerFilter> commandHandlerFilters;

    public ProtocolCommandHandlerFilterFactory(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
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
        return AwareHelper.enrichIfNecessary(Lists.newArrayList(ExtensionManager.getOrLoadExtensions(ProtocolCommandHandlerFilter.class)), brokerContext);
    }
}