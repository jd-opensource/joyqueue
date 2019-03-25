package com.jd.journalq.toolkit.config;

import com.jd.journalq.toolkit.config.annotation.ObjectBinding;
import com.jd.journalq.toolkit.reflect.Reflect;
import com.jd.journalq.toolkit.reflect.ReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 对象绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class ObjectBinder implements Binder {
    public static final ObjectBinder INSTANCE = new ObjectBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                ObjectBinding)) {
            return;
        }
        ObjectBinding binding = (ObjectBinding) annotation;
        Class<?> type = field.getType();
        Object value = context.getObject(binding.key());
        if (value != null && type.isInstance(value)) {
            Reflect.set(field, target, value);
        } else {
            Reflect.set(field, target, null);
        }
    }
}
