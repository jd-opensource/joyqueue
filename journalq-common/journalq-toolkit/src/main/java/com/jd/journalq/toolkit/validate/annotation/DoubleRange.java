package com.jd.journalq.toolkit.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 浮点数范围检查
 * Created by hexiaofeng on 15-7-20.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleRange {

    /**
     * 最小值
     *
     * @return 最小值
     */
    double min() default Double.MIN_VALUE;

    /**
     * 最大值
     *
     * @return 最大值
     */
    double max() default Double.MAX_VALUE;

    /**
     * 错误提示
     *
     * @return 消息
     */
    String message() default "";

}
