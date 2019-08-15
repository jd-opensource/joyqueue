package io.chubao.joyqueue.toolkit.validate;

import io.chubao.joyqueue.toolkit.validate.annotation.NotNull;

import java.lang.annotation.Annotation;

/**
 * 验证不能为Null
 * Created by hexiaofeng on 16-5-10.
 */
public class NotNullValidator implements Validator {

    public static final NotNullValidator INSTANCE = new NotNullValidator();

    @Override
    public void validate(final Object target, final Annotation annotation, final Value value) throws ValidateException {
        NotNull notNull = (NotNull) annotation;
        if (value.value == null) {
            if (notNull.message() == null || notNull.message().isEmpty()) {
                throw new ValidateException(value.name + " can not be null.");
            }
            throw new ValidateException(String.format(notNull.message(), value.name));
        }
    }
}
