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
package org.joyqueue.network.transport.support;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.network.transport.ChannelTransport;
import org.joyqueue.network.transport.RequestBarrier;
import org.joyqueue.network.transport.ResponseFuture;
import org.joyqueue.network.transport.TransportAttribute;
import org.joyqueue.network.transport.TransportState;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.config.TransportConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * DefaultChannelTransport
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class DefaultChannelTransport implements ChannelTransport {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultChannelTransport.class);

    private Channel channel;
    private TransportAttribute attribute = new DefaultTransportAttribute();
    private RequestBarrier barrier;
    private TransportConfig config;
    private SocketAddress address;

    public DefaultChannelTransport(Channel channel, RequestBarrier barrier) {
        this.channel = channel;
        this.barrier = barrier;
        this.config = barrier.getConfig();
        this.address = channel.remoteAddress();
    }

    public DefaultChannelTransport(Channel channel, RequestBarrier barrier, SocketAddress address) {
        this.channel = channel;
        this.barrier = barrier;
        this.config = barrier.getConfig();
        this.address = address;
    }

    public DefaultChannelTransport(Channel channel, TransportAttribute attribute, RequestBarrier barrier, SocketAddress address) {
        this.channel = channel;
        this.attribute = attribute;
        this.barrier = barrier;
        this.config = barrier.getConfig();
        this.address = address;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Command sync(final Command command) throws TransportException {
        return sync(command, 0);
    }

    @Override
    public Command sync(final Command command, final long timeout) throws TransportException {
        if (command == null) {
            throw new IllegalArgumentException("The argument command must not be null");
        }
        long sendTimeout = timeout <= 0 ? barrier.getSendTimeout() : timeout;
        // 同步调用
        ResponseFuture future = new ResponseFuture(this, command, sendTimeout, null,
                null, null, new CountDownLatch(1));
        barrier.putSyncFuture(command.getHeader().getRequestId(), future);
        // 发送数据,应答成功回来或超时会自动释放command
        channel.writeAndFlush(command).addListener(new ResponseListener(future, barrier));

        try {
            // 等待命令返回
            Command response = future.await();
            if (null == response) {
                // 发送请求成功，等待应答超时
                if (future.isSuccess()) {
                    throw TransportException.RequestTimeoutException.build(IpUtil.toAddress(address));
                } else {
                    // 发送请求失败
                    Throwable cause = future.getCause();
                    if (cause != null) {
                        throw cause;
                    }
                    throw TransportException.RequestErrorException.build(IpUtil.toAddress(address));
                }
            }

            return response;
        } catch (Throwable e) {
            future.release(e, false);
            // 出现异常
            barrier.remove(command.getHeader().getRequestId());
            if (e instanceof TransportException) {
                throw (TransportException) e;
            } else if (e instanceof InterruptedException) {
                throw TransportException.InterruptedException.build();
            } else {
                throw TransportException.RequestErrorException.build("请求错误, " + address, e);
            }
        }
    }

    @Override
    public void async(final Command command, final CommandCallback callback) throws TransportException {
        async(command, 0, callback);
    }

    @Override
    public void async(final Command command, final long timeout, CommandCallback callback) throws
            TransportException {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }

        long sendTimeout = timeout <= 0 ? barrier.getSendTimeout() : timeout;
        // 获取信号量
        if (!config.isNonBlockAsync()) {
            barrier.acquire(RequestBarrier.SemaphoreType.ASYNC, sendTimeout);
        }

        try {
            long time = SystemClock.now();
            time = SystemClock.now() - time;
            sendTimeout = (int) (sendTimeout - time);
            sendTimeout = sendTimeout < 0 ? 0 : sendTimeout;

            // 发送请求
            ResponseFuture future =
                    new ResponseFuture(this, command, sendTimeout, callback, barrier, RequestBarrier.SemaphoreType.ASYNC, null);
            if (barrier.get(command.getHeader().getRequestId()) != null) {
                logger.warn("async command(type {}, request id {}) already exist",
                        command.getHeader().getType(), command.getHeader().getRequestId());
            }
            barrier.putAsyncFuture(command.getHeader().getRequestId(), future);
            // 应答回来的时候或超时会自动释放command
            channel.writeAndFlush(command).addListener(new ResponseListener(future, barrier));

        } catch (Throwable th) {
            logger.warn("Default channel transport async fail, command type is {}", command.getHeader().getType(), th);
            if (!config.isNonBlockAsync()) {
                barrier.release(RequestBarrier.SemaphoreType.ASYNC);
            }
            barrier.remove(command.getHeader().getRequestId());
            command.release();
            throw th;
        }
    }

    @Override
    public CompletableFuture<?> async(Command command) throws TransportException {
        return async(command, 0);
    }

    @Override
    public CompletableFuture<?> async(Command command, long timeout) throws TransportException {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        CompletableFuture completableFuture = new CompletableFuture();
        async(command, timeout, new CompletableFutureCallback(completableFuture));
        return completableFuture;
    }

    @Override
    public void oneway(final Command command) throws TransportException {
        oneway(command, 0);
    }

    @Override
    public void oneway(final Command command, final long timeout) throws TransportException {
        if (command == null) {
            throw new IllegalArgumentException("The argument command must not be null");
        }

        // 不需要应答
        command.getHeader().setQosLevel(QosLevel.ONE_WAY);
        ResponseFuture future = null;

        long sendTimeout = timeout <= 0 ? barrier.getSendTimeout() : timeout;
        long time = SystemClock.now();
        // 获取信号量
        if (!config.isNonBlockOneway()) {
            barrier.acquire(RequestBarrier.SemaphoreType.ONEWAY, sendTimeout);
        }

        try {
            // 如果非阻塞，发送完不处理
            if (config.isNonBlockOneway()) {
                channel.writeAndFlush(command);
                return;
            }

            time = SystemClock.now() - time;
            sendTimeout = (int) (sendTimeout - time);
            sendTimeout = sendTimeout < 0 ? 0 : sendTimeout;

            // 发送请求
            future = new ResponseFuture(this, command, sendTimeout, null, barrier, RequestBarrier.SemaphoreType.ONEWAY,
                    new CountDownLatch(1));
            // 命令执行成功或超时则会自动释放command
            channel.writeAndFlush(command).addListener(new OnewayListener(future));

            // 确保处理完成
            future.await();
            // 后续会在Listener中自动释放Future
            if (!future.isSuccess()) {
                // 发送请求失败
                Throwable cause = future.getCause();
                if (cause != null) {
                    if (cause instanceof TransportException) {
                        throw (TransportException) cause;
                    }
                    throw TransportException.RequestErrorException.build(cause);
                }
                throw TransportException.RequestErrorException.build();
            }
        } catch (Throwable th) {
            if (th instanceof TransportException) {
                // 防止在acquireSemaphore获取异常
                command.release();
                throw (TransportException) th;
            } else if (th instanceof InterruptedException) {
                TransportException.InterruptedException ex = TransportException.InterruptedException.build();
                if (future != null) {
                    future.release(ex, false);
                }
                throw ex;
            }

            if (!config.isNonBlockOneway()) {
                barrier.release(RequestBarrier.SemaphoreType.ONEWAY);
            }
        }
    }

    @Override
    public void acknowledge(Command request, Command response) throws TransportException {
        acknowledge(request, response, null);
    }

    @Override
    public void acknowledge(Command request, Command response, CommandCallback callback) throws TransportException {
        if (response == null) {
            return;
        }

        if (request != null) {
            Header header = request.getHeader();
            if (header != null) {
                if (response.getHeader() == null) {
                    response.setHeader(request.getHeader());
                    response.getHeader().setDirection(Direction.RESPONSE);
                }
                if (response.getHeader().getQosLevel() == null) {
                    response.getHeader().setQosLevel(QosLevel.RECEIVE);
                }
                if (response.getHeader().getDirection() == null) {
                    response.getHeader().setDirection(Direction.RESPONSE);
                }
                if (response.getHeader().getType() == request.getHeader().getType()) {
                    if (response.getPayload() instanceof Type) {
                        response.getHeader().setType(((Type) response.getPayload()).type());
                    }
                }
                response.getHeader().setRequestId(header.getRequestId());

                // 判断请求是否要应答
                if (header.getQosLevel() == QosLevel.ONE_WAY) {
                    // 不用应答，释放资源
                    request.release();
                    // 回调
                    if (callback != null) {
                        try {
                            callback.onSuccess(request, response);
                        } catch (Exception ignored) {
                        }
                    }
                    return;
                }
            }
        }

        channel.writeAndFlush(response)
                .addListener(new CallbackListener(request, response, callback))
                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public SocketAddress remoteAddress() {
        if (address == null) {
            return channel.remoteAddress();
        } else {
            return address;
        }
    }

    @Override
    public TransportAttribute attr() {
        return attribute;
    }

    @Override
    public void attr(TransportAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public TransportState state() {
        if (channel.isActive()) {
            return TransportState.CONNECTED;
        } else {
            return TransportState.DISCONNECTED;
        }
    }

    @Override
    public void stop() {
        channel.close();
    }

    @Override
    public String toString() {
        return channel.toString();
    }

    /**
     * 异步请求监听器
     */
    protected  abstract static class FutureListener implements ChannelFutureListener {

        protected static final Logger logger = LoggerFactory.getLogger(FutureListener.class);

        protected ResponseFuture response;

        public FutureListener(ResponseFuture response) {
            this.response = response;
        }

        /**
         * 输出日志
         *
         * @param channel 通道
         */
        protected void logError(final Channel channel) {
            // 打印日志
            String error = "send a request command to " + IpUtil.toAddress(channel.remoteAddress()) + " failed.";
            Throwable cause = response.getCause();
            if (cause != null) {
                if (cause instanceof ClosedChannelException) {
                    // 连接关闭了，则忽略该异常
                } else {
                    logger.error(error, cause);
                }
            } else {
                logger.error(error);
            }
        }

    }

    /**
     * 等待应答监听器
     */
    protected static class ResponseListener extends FutureListener {

        private RequestBarrier barrier;

        public ResponseListener(ResponseFuture response, RequestBarrier barrier) {
            super(response);
            this.barrier = barrier;
        }

        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {
            // 获取命令监听器
            response.setSuccess(future.isSuccess());
            if (response.isSuccess()) {
                // 请求成功，等待应答回调，目前请求命令占用的资源可以释放了
                Command request = response.getRequest();
                if (request != null) {
                    request.release();
                }
            } else {
                // 出错
                response.setCause(future.cause());
                response.setResponse(null);
                response.release(null, true);

                barrier.remove(response.getRequestId());
                // 关闭连接
                Channel channel = future.channel();
                channel.close();
                // 输出日志
                logError(channel);
            }

        }


    }

    /**
     * Oneway监听器
     */
    protected static class OnewayListener extends FutureListener {

        public OnewayListener(ResponseFuture response) {
            super(response);
        }

        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {
            // 获取命令监听器
            response.setSuccess(future.isSuccess());
            response.setCause(future.cause());
            response.setResponse(null);
            response.release(null, true);
            // 没有存放到futures中
            if (!response.isSuccess()) {
                Channel channel = future.channel();
                channel.close();
                logError(channel);
            }
        }
    }

    protected static class CallbackListener implements ChannelFutureListener {

        private Command request;
        private Command response;
        private CommandCallback callback;

        public CallbackListener(Command request, Command response, CommandCallback callback) {
            this.request = request;
            this.response = response;
            this.callback = callback;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (callback != null) {
                if (future.isSuccess()) {
                    callback.onSuccess(request, response);
                } else {
                    callback.onException(request, null);
                }
            }
            request.release();
        }
    }

    protected static class CompletableFutureCallback implements CommandCallback {

        private CompletableFuture completableFuture;

        public CompletableFutureCallback(CompletableFuture completableFuture) {
            this.completableFuture = completableFuture;
        }

        @Override
        public void onSuccess(Command request, Command response) {
            completableFuture.complete(response);
        }

        @Override
        public void onException(Command request, Throwable cause) {
            completableFuture.completeExceptionally(cause);
        }
    }
}