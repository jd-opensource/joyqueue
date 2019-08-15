package io.chubao.joyqueue.handler.message;

import io.chubao.joyqueue.service.OperLogService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.MessageHandler;
import io.vertx.core.eventbus.Message;

/**
 * 操作日志消息处理器
 * Created by chenyanying on 19-3-3.
 */
public class OperLogMessageHandler implements MessageHandler<OperLogMessage> {

    @Value(nullable=false)
    protected OperLogService operLogService;

    @Override
    public String type() {
        return "operLogMessage";
    }

    @Override
    public void handle(Message<OperLogMessage> event) {
        OperLogMessage operLogMsg = event.body();
        if (operLogMsg == null ) {
            return;
        }
        operLogService.add(operLogMsg);
    }
}
