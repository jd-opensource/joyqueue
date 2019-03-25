package com.jd.journalq.nsr.network;

import com.jd.journalq.common.network.transport.command.handler.CommandHandler;
import com.jd.journalq.nsr.NameServiceAware;

/**
 * @author wylixiaobin
 * Date: 2019/3/14
 */
public interface NsrCommandHandler extends CommandHandler ,NameServiceAware {
    public static final String SERVER_TYPE="SERVER";
    public static final String THIN_TYPE="THIN";
}
