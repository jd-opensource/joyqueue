/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.handler.message;

import com.jd.laf.web.vertx.MessageHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志消息处理器
 * Created by yangyang115 on 18-7-30.
 */
public class AuditLogMessageHandler implements MessageHandler<AuditLogMessage> {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogMessageHandler.class);
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String type() {
        return "auditLogMessage";
    }

    @Override
    public void handle(final Message<AuditLogMessage> message) {

        if (message != null) {
            AuditLogMessage auditLog = message.body();
            Instant instant = auditLog.getTime().toInstant();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            if (auditLog.getTarget() != null) {
                logger.info(
                        String.format("%s在时间点%s为%s%s%s", auditLog.getUser(), localDateTime.format(FORMAT),
                                auditLog.getTarget(), auditLog.getType().description(), auditLog.message));
            } else {
                logger.info(String.format("%s在时间点%s%s%s", auditLog.getUser(), localDateTime.format(FORMAT),
                        auditLog.getType().description(), auditLog.message));
            }
        }
    }

}
