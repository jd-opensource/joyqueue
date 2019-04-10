package com.jd.journalq.handler.error;

/**
 * Identifier exception conversion
 * Created by chenyanying3 on 18-11-16.
 */
public class IdentifierException extends ConfigException {

    public IdentifierException(int maxLength) {
        super(ErrorCode.BadRequest, "标识符必须以英文字母开头，英文字母、阿拉伯数字、下划线和小数点组成，最大长度为" + maxLength);
    }
}
