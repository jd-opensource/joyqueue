package com.jd.journalq.toolkit.config;

import com.jd.journalq.toolkit.reflect.ReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 绑定器
 * Created by hexiaofeng on 16-8-29.
 */
public interface Binder {

    /**
     * 绑定对象
     *
     * @param field      自动
     * @param annotation 声明
     * @param target     对象
     * @param context    上下文
     * @throws ReflectException
     */
    void bind(Field field, Annotation annotation, Object target, Context context) throws ReflectException;
}
