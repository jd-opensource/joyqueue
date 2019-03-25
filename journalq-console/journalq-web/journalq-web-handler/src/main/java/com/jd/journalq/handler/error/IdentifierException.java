package com.jd.journalq.handler.error;

/**
 * 标识符异常
 */
public class IdentifierException extends ConfigException {

    public IdentifierException(int maxLength) {
        super(ErrorCode.BadRequest, "标识符必须以英文字母开头，英文字母、阿拉伯数字、下划线和小数点组成，最大长度为" + maxLength);
    }
}
