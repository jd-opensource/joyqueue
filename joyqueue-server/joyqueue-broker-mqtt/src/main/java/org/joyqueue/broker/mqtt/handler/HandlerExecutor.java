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
package org.joyqueue.broker.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author majun8
 */
public class HandlerExecutor implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(HandlerExecutor.class);

    private Handler handler;
    private ChannelHandlerContext context;
    private MqttMessage message;

    public HandlerExecutor(final Handler handler, final ChannelHandlerContext context, final MqttMessage message) {
        this.handler = handler;
        this.context = context;
        this.message = message;
    }

    @Override
    public void run() {
        execute();
    }

    public void execute() {
        try {
            if (handler != null) {
                handler.handleRequest(context.channel(), message);
            }
        } catch (Throwable th) {
            LOG.error("HandlerExecutor got exception: ", th);
            context.fireExceptionCaught(th);
        } finally {
            ReferenceCountUtil.release(message);
        }
    }
}
