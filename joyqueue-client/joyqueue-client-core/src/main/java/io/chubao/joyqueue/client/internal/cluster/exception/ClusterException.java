package io.chubao.joyqueue.client.internal.cluster.exception;

import io.chubao.joyqueue.client.internal.exception.ClientException;

/**
 * ClusterException
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class ClusterException extends ClientException {

    public ClusterException() {
    }

    public ClusterException(String message) {
        super(message);
    }

    public ClusterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClusterException(Throwable cause) {
        super(cause);
    }

    public ClusterException(String error, int code) {
        super(error, code);
    }

    public ClusterException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }
}