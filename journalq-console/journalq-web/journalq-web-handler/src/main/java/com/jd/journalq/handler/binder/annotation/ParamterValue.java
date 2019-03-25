package com.jd.journalq.handler.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 值
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamterValue {
    String value() default "";
    /**
     * 是否可以为空
     *
     * @return 为空标识
     */
    boolean nullable() default true;

    /**
     * 默认值
     *
     * @return 默认值
     */
    String defaultValue() default "";
}
