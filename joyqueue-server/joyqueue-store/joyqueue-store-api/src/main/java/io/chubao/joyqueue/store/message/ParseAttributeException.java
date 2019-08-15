package io.chubao.joyqueue.store.message;

/**
 * 解析消息异常
 * @author liyue25
 * Date: 2019-01-15
 */
public class ParseAttributeException extends RuntimeException {
    public ParseAttributeException(String message) {
        super(message);
    }
}
