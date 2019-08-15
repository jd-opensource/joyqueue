package io.chubao.joyqueue.client.internal.metadata.exception;

import io.chubao.joyqueue.client.internal.exception.ClientException;

/**
 * MetadataException
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class MetadataException extends ClientException {

    public MetadataException() {
    }

    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataException(Throwable cause) {
        super(cause);
    }

    public MetadataException(String error, int code) {
        super(error, code);
    }

    public MetadataException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }
}