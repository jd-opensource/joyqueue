package io.chubao.joyqueue.toolkit.config;

import io.chubao.joyqueue.toolkit.reflect.Reflect;
import io.chubao.joyqueue.toolkit.reflect.ReflectException;
import io.chubao.joyqueue.toolkit.config.annotation.StringBinding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 字符串绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class StringBinder implements Binder {
    public static final StringBinder INSTANCE = new StringBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                StringBinding)) {
            return;
        }
        StringBinding binding = (StringBinding) annotation;
        Class<?> type = field.getType();
        if (type.isAssignableFrom(String.class)) {
            String value = context.getString(binding.key(), binding.defaultValue());
            Reflect.set(field, target, value);
        }
    }
}
