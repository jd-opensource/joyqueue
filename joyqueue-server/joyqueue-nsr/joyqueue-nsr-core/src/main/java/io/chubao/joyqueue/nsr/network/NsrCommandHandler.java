package io.chubao.joyqueue.nsr.network;

import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.nsr.NameServiceAware;

/**
 * @author wylixiaobin
 * Date: 2019/3/14
 */
public interface NsrCommandHandler extends CommandHandler ,NameServiceAware {
    String SERVER_TYPE="SERVER";
    String THIN_TYPE="THIN";
}
