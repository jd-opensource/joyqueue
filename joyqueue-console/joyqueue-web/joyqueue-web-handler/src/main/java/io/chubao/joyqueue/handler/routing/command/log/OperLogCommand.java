package io.chubao.joyqueue.handler.routing.command.log;

import io.chubao.joyqueue.handler.routing.command.CommandSupport;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QOperLog;
import io.chubao.joyqueue.service.OperLogService;

/**
 * Created by wangxiaofei1 on 2018/12/3.
 */
public class OperLogCommand extends CommandSupport<OperLog,OperLogService,QOperLog> {

}
