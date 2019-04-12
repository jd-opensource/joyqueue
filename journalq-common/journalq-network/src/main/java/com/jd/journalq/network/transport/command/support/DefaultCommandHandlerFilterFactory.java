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
package com.jd.journalq.network.transport.command.support;

import com.google.common.collect.Lists;
import com.jd.journalq.network.transport.command.handler.filter.CommandHandlerFilter;
import com.jd.journalq.network.transport.command.handler.filter.CommandHandlerFilterFactory;
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