package io.chubao.joyqueue.toolkit.validate;

import io.chubao.joyqueue.toolkit.reflect.Reflect;
import io.chubao.joyqueue.toolkit.validate.annotation.Size;

import java.lang.annotation.Annotation;

/**
 * 验证不能为空，支持字符序列，集合，散列，数组
 * Created by hexiaofeng on 16-5-10.
 */
public class SizeValidator implements Validator {

    public static final SizeValidator INSTANCE = new SizeValidator();

    @Override
    public void validate(final Object target, final Annotation annotation, final Value value) throws ValidateException {
        Size size = (Size) annotation;
        int length = Reflect.size(value.type, value.value);
        if (length < size.min() || length > size.max()) {
            if (size.message() == null || size.message().isEmpty()) {
                throw new ValidateException(
                        String.format("%s length must between %d and %d.", value.name, size.min(), size.max()));
            }
            throw new ValidateException(String.format(size.message(), value.name, size.min(), size.max()));
        }
    }
}
