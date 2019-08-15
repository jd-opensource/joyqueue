package io.chubao.joyqueue.nsr.network;

import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.NameServiceAware;
import io.chubao.joyqueue.nsr.NsrPlugins;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
abstract class NsrCommandHandlerFactory extends DefaultCommandHandlerFactory {
    public abstract String getType();

    public abstract void doWithHandler(NsrCommandHandler nsrCommandHandler);

    public void register(NameService nameService) {
        NsrPlugins.nsrCommandHandlerPlugins.metas(getType()).forEach(meta -> {
            NsrCommandHandler commandHandler = meta.getTarget();
            enrichIfNecessary(commandHandler, nameService);
            register(commandHandler);
            doWithHandler(meta.getTarget());
        });
    }

    public static void enrichIfNecessary(Object obj, NameService nameService) {
        if (obj == null) {
            return;
        }
        if (obj instanceof NameServiceAware) {
            ((NameServiceAware) obj).setNameService(nameService);
        }
    }
}
