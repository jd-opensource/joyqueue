package com.jd.journalq.nsr.network;

import com.jd.journalq.common.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.nsr.NameServiceAware;
import com.jd.journalq.nsr.NsrPlugins;

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
            enrichIfNecessary(commandHandler,nameService);
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
