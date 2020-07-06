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
package org.joyqueue.broker.kafka.network.protocol;

import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;

/**
 * KafkaExceptionHandler
 *
 * author: gaohaoxiang
 * date: 2019/3/27
 */
public class KafkaExceptionHandler implements ExceptionHandler {

    protected final Logger logger = LoggerFactory.getLogger(KafkaExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
    	if (throwable instanceof RejectedExecutionException) {
    		logger.error("kafka exception, command: {}, transport: {}, exception: {}", command, transport, throwable.getMessage());
    	} else {
    		logger.error("kafka exception, command: {}, transport: {}", command, transport, throwable);
    	}
        transport.stop();
    }
}