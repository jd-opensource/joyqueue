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
package org.joyqueue.network.transport.command.support;

import org.joyqueue.network.transport.RequestBarrier;
import org.joyqueue.network.transport.ResponseFuture;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.config.TransportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResponseHandler
 *
 * author: gaohaoxiang
 * date: 2018/8/24
 */
public class ResponseHandler {

    protected static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private TransportConfig config;
    private RequestBarrier barrier;
    private ExceptionHandler exceptionHandler;

    public ResponseHandler(TransportConfig transportConfig, RequestBarrier barrier, ExceptionHandler exceptionHandler) {
        this.config = transportConfig;
        this.barrier = barrier;
        this.exceptionHandler = exceptionHandler;
    }

    public void handle(Transport transport, Command response) {
        Header header = response.getHeader();
        // 超时被删除了
        final ResponseFuture responseFuture = barrier.get(header.getRequestId());
        if (responseFuture == null) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("request is timeout %s", header));
            }
            return;
        }
        // 设置应答
        responseFuture.setResponse(response);
        // 异步调用
        if (responseFuture.getCallback() != null) {
            boolean success = false;
            try {
                barrier.onAsyncFuture(responseFuture);
                success = true;
            } catch (Throwable e) {
                logger.error("execute callback error.", e);
            }

            if (!success) {
                try {
                    responseFuture.onSuccess();
                } catch (Throwable e) {
                    logger.error("execute callback error.", e);
                } finally {
                    responseFuture.release();
                }
            }
        } else {
            // 释放资源，不回调
            if (!responseFuture.release()) {
                // 已经被释放了
                return;
            }
        }
        barrier.remove(header.getRequestId());
    }
}