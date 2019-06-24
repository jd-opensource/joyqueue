/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.joyqueue.broker.network.protocol;

import com.google.common.collect.Lists;
import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.helper.AwareHelper;
import com.jd.joyqueue.network.transport.command.handler.filter.CommandHandlerFilter;
import com.jd.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.joyqueue.network.transport.command.support.CommandHandlerFilterComparator;
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