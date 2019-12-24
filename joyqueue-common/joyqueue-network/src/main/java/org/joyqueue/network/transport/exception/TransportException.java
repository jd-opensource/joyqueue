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
package org.joyqueue.network.transport.exception;

import java.io.IOException;

/**
 * TransportException
 * Created by hexiaofeng on 16-6-22.
 */
public class TransportException extends RuntimeException {

    public static final String UNKNOWN_HOST = "无限主机";
    public static final String CONNECTION_TIMEOUT = "连接超时";
    public static final String REQUEST_TIMEOUT = "请求超时";
    public static final String CONNECTION_ERROR = "连接错误";
    public static final String ILLEGAL_STATE = "状态无效";
    public static final String REQUEST_ERROR = "请求错误";
    public static final String THREAD_EXHAUST = "线程耗尽";
    public static final String NO_PERMISSION = "无权限";
    public static final String INVALID_PROTOCOL = "无效协议";
    public static final String REQUEST_EXCESSIVE = "请求过多";
    public static final String THREAD_INTERRUPTED = "线程被终止";
    public static final String CODEC_EXCEPTION = "解码出错";

    protected int code;

    public TransportException(Throwable cause) {
        super(cause);
    }

    public TransportException(String message, int code) {
        super(message);
        this.code = code;
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public TransportException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 连接异常
     */
    public static class ConnectionException extends TransportException {
        public ConnectionException() {
            super(CONNECTION_ERROR, 20);
        }

        public ConnectionException(String message) {
            super(message, 20);
        }

        public ConnectionException(String message, int code) {
            super(message, code);
        }

        public ConnectionException(String message, Throwable t) {
            super(message, t);
        }

        public static ConnectionException build(String address) {
            return new ConnectionException(String.format(CONNECTION_ERROR + ",%s", address));
        }
    }

    /**
     * 未知域名异常
     */
    public static class UnknownHostException extends ConnectionException {
        public UnknownHostException() {
            super(UNKNOWN_HOST);
        }

        public UnknownHostException(String message) {
            super(message);
        }

        public static UnknownHostException build(String address) {
            return new UnknownHostException(String.format(UNKNOWN_HOST + ",%s", address));
        }
    }

    /**
     * 连接超时异常
     */
    public static class ConnectionTimeoutException extends ConnectionException {
        public ConnectionTimeoutException() {
            super(CONNECTION_TIMEOUT, 21);
        }

        public ConnectionTimeoutException(String message) {
            super(message, 21);
        }

        public static ConnectionTimeoutException build(String address) {
            return new ConnectionTimeoutException(String.format(CONNECTION_TIMEOUT + ",%s", address));
        }
    }

    /**
     * 请求异常
     */
    public static class RequestErrorException extends TransportException {
        public RequestErrorException() {
            super(REQUEST_ERROR, 23);
        }

        public RequestErrorException(String message) {
            super(message, 23);
        }

        protected RequestErrorException(String message, int code) {
            super(message, code);
        }

        public RequestErrorException(Throwable cause) {
            super(REQUEST_ERROR, cause, 23);
        }

        public RequestErrorException(String message, Throwable cause) {
            super(message, cause, 23);
        }

        public static RequestErrorException build() {
            return new RequestErrorException();
        }

        public static RequestErrorException build(Throwable cause) {
            return new RequestErrorException(cause);
        }

        public static RequestErrorException build(String message) {
            return new RequestErrorException(message);
        }

        public static RequestErrorException build(String message, Throwable cause) {
            return new RequestErrorException(message, cause);
        }
    }

    /**
     * 请求超时异常
     */
    public static class RequestTimeoutException extends RequestErrorException {
        public RequestTimeoutException() {
            super(REQUEST_TIMEOUT, 21);
        }

        public RequestTimeoutException(String message) {
            super(message, 21);
        }

        public static RequestTimeoutException build(String address) {
            return new RequestTimeoutException(String.format(REQUEST_TIMEOUT + ",%s", address));
        }
    }

    /**
     * 并发请求过多异常
     */
    public static class RequestExcessiveException extends RequestErrorException {
        public RequestExcessiveException() {
            super(REQUEST_EXCESSIVE, 24);
        }

        public static RequestExcessiveException build() {
            return new RequestExcessiveException();
        }
    }

    /**
     * 未知错误
     */
    public static class UnknownException extends TransportException {
        public UnknownException() {
            super(REQUEST_TIMEOUT, 4);
        }

        public UnknownException(String message) {
            super(message, 4);
        }

        public UnknownException(String message, Throwable cause) {
            super(message, cause, 4);
        }

        public static UnknownException build(String message) {
            return new UnknownException(message);
        }

        public static UnknownException build(Throwable cause) {
            return new UnknownException(cause.getMessage(), cause);
        }
    }

    /**
     * 服务没有启动
     */
    public static class IllegalStateException extends TransportException {
        public IllegalStateException() {
            super(ILLEGAL_STATE, 9);
        }

        public static IllegalStateException build() {
            return new IllegalStateException();
        }

    }

    /**
     * 线程被终止
     */
    public static class InterruptedException extends TransportException {
        public InterruptedException() {
            super(THREAD_INTERRUPTED, 25);
        }

        public static InterruptedException build() {
            return new InterruptedException();
        }
    }

    /**
     * 线程被耗尽，无法执行请求命令异常
     */
    public static class ThreadExhaustException extends TransportException {
        public ThreadExhaustException() {
            super(THREAD_EXHAUST, 26);
        }

        public static ThreadExhaustException build() {
            return new ThreadExhaustException();
        }
    }

    /**
     * 不支持的协议
     */
    public static class InvalidProtocolException extends TransportException {
        public InvalidProtocolException() {
            super(INVALID_PROTOCOL, 29);
        }

        public static InvalidProtocolException build() {
            return new InvalidProtocolException();
        }
    }

    /**
     * 无权限
     */
    public static class NoPermissionException extends TransportException {
        public NoPermissionException() {
            super(NO_PERMISSION, 1);
        }

        public NoPermissionException(String message) {
            super(message, 1);
        }

        public static NoPermissionException build() {
            return new NoPermissionException();
        }

        public static NoPermissionException build(String address) {
            return new NoPermissionException(String.format(NO_PERMISSION + ",%s", address));
        }
    }

    /**
     * 编解码异常
     */
    public static class CodecException extends TransportException {
        public CodecException() {
            super(CODEC_EXCEPTION, 28);
        }

        public CodecException(String message) {
            super(message, 28);
        }

        public CodecException(Throwable cause) {
            super(cause);
        }

        public static CodecException build() {
            return new CodecException();
        }

        public static CodecException build(String message) {
            return new CodecException(message);
        }

    }

    public static boolean isClosed(Throwable e) {
        if (e == null) {
            return false;
        }
        if (e instanceof IOException) {
            String message = e.getMessage();
            if (message.contains("Connection reset") || message.contains("Socket closed") || message
                    .contains("连接被对端重置") || message.contains("Connection refused")) {
                return true;
            }
        }
        return false;
    }
}
