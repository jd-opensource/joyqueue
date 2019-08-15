package io.chubao.joyqueue.exception;

import io.chubao.joyqueue.model.exception.DataException;

/**
 * 服务的基础异常
 *
 * @author zhuchunlai
 * @version 2.0.0
 * @since 2015-03-02
 */
public class ServiceException extends DataException {
    /**
     * 输入参数错误
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 认证失败
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 服务禁止访问
     */
    public static final int FORBIDDEN = 403;

    /**
     * 数据不存在，如用户不存在等
     */
    public static final int NOT_FOUND = 404;

    /**
     * 发生未知错误
     */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * I/O错误
     */
    public static final int IO_ERROR = 503;

    /**
     * 并发事务错误
     */
    public static final int CONCURRENCY_ERROR = 504;
    /**
     * 第三方异常
     */
    public static final int THIRD_PARTY_ERROR = 600;

    public static final int IGNITE_RPC_ERROR = 10001;


    protected ServiceException() {
    }

    public ServiceException(final int status, final String message) {
        super(message);
        this.status = status;
    }

    public ServiceException(final int status, final String message, final Throwable cause) {
        super(message, cause);
        this.status = status;
    }

}
