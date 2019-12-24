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

import org.joyqueue.service.OperLogService;
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
