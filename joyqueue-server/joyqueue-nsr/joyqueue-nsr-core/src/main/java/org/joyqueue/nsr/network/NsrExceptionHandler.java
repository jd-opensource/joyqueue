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
package org.joyqueue.nsr.network;

import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrExceptionHandler implements ExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(NsrExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        if (TransportException.isClosed(throwable)) {
            logger.warn("channel close, address: {}, message: {}", transport.remoteAddress(), throwable.getMessage());
        } else {
            logger.error("nameserver exception, transport: {}, command: {}", transport, command, throwable);
        }
        transport.stop();
    }
}
