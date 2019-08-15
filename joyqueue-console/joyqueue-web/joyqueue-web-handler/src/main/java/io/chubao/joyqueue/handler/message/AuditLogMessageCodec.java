package io.chubao.joyqueue.handler.message;

import com.jd.laf.web.vertx.message.JsonMessageCodec;

/**
 * 审计日志消息编解码处理器
 * Created by chenyanying3 on 19-3-3.
 */
public class AuditLogMessageCodec extends JsonMessageCodec<AuditLogMessage> {

    public AuditLogMessageCodec() {
        super(AuditLogMessage.class);
    }
}
