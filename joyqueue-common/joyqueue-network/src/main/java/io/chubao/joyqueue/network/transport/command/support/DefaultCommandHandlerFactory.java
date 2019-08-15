package io.chubao.joyqueue.network.transport.command.support;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * DefaultCommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public class DefaultCommandHandlerFactory implements CommandHandlerFactory {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultCommandHandlerFactory.class);

    private Map<Integer /** type **/, CommandHandler> commandHandlerMapper = Maps.newHashMap();

    @Override
    public CommandHandler getHandler(Command command) {
        int commandType = command.getHeader().getType();
        return commandHandlerMapper.get(commandType);
    }

    public void register(CommandHandler commandHandler) {
        if (commandHandler instanceof Types) {
            Types types = (Types) commandHandler;
            for (int type : types.types()) {
                register(type, commandHandler);
            }
        } else if (commandHandler instanceof Type) {
            register(((Type) commandHandler).type(), commandHandler);
        } else {
            logger.warn("unsupported command type, commandHandler: {}", commandHandler);
        }
    }

    public void register(int type, CommandHandler commandHandler) {
        commandHandlerMapper.put(type, commandHandler);
    }
}