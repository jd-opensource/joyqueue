package com.jd.journalq.toolkit.config;

import com.jd.journalq.toolkit.config.annotation.NumberBinding;
import com.jd.journalq.toolkit.reflect.Reflect;
import com.jd.journalq.toolkit.reflect.ReflectException;
import com.jd.journalq.toolkit.validate.annotation.Range;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 数值绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class NumberBinder implements Binder {
    public static final NumberBinder INSTANCE = new NumberBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                NumberBinding)) {
            return;
        }
        NumberBinding binding = (NumberBinding) annotation;
        Class<?> type = field.getType();
        Number number = null;
        Range range = binding.range();
        if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
            number = context.getByte(binding.key(), (byte) binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range
                    .max()) ? (byte) binding.defaultValue() : number;
            Reflect.set(field, target, number);
        } else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
            number = context.getShort(binding.key(), (short) binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range
                    .max()) ? (short) binding.defaultValue() : number;
            Reflect.set(field, target, number);
        } else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            number = context.getInteger(binding.key(), (int) binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range
                    .max()) ? (int) binding.defaultValue() : number;
            Reflect.set(field, target, number);
        } else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
            number = context.getLong(binding.key(), binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range.max()) ? binding
                    .defaultValue() : number;
            Reflect.set(field, target, number);
        }
    }
}
