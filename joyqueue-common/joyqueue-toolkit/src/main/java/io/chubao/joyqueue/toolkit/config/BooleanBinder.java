package io.chubao.joyqueue.toolkit.config;

import io.chubao.joyqueue.toolkit.config.annotation.BooleanBinding;
import io.chubao.joyqueue.toolkit.reflect.Reflect;
import io.chubao.joyqueue.toolkit.reflect.ReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 布尔值绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class BooleanBinder implements Binder {
    public static final BooleanBinder INSTANCE = new BooleanBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                BooleanBinding)) {
            return;
        }
        BooleanBinding binding = (BooleanBinding) annotation;
        Class<?> type = field.getType();
        if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
            boolean value = context.getBoolean(binding.key(), binding.defaultValue());
            Reflect.set(field, target, value);
        }
    }
}
