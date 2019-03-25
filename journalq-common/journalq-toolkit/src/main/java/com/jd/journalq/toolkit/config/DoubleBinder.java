package com.jd.journalq.toolkit.config;

import com.jd.journalq.toolkit.config.annotation.DoubleBinding;
import com.jd.journalq.toolkit.reflect.Reflect;
import com.jd.journalq.toolkit.reflect.ReflectException;
import com.jd.journalq.toolkit.validate.annotation.DoubleRange;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 浮点数绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class DoubleBinder implements Binder {
    public static final DoubleBinder INSTANCE = new DoubleBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                DoubleBinding)) {
            return;
        }
        DoubleBinding binding = (DoubleBinding) annotation;
        Class<?> type = field.getType();
        if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
            double value = context.getDouble(binding.key(), binding.defaultValue());
            DoubleRange range = binding.range();
            if (range != null && (value < range.min() || value > range.max())) {
                value = binding.defaultValue();
            }
            Reflect.set(field, target, value);
        }
    }
}
