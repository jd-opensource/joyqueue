package io.chubao.joyqueue.toolkit.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字符串、数组、集合、散列大小
 * Created by hexiaofeng on 15-7-20.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {

    /**
     * 最小值
     *
     * @return 最小值
     */
    int min() default 0;

    /**
     * 最大值
     *
     * @return 最大值
     */
    int max() default Integer.MAX_VALUE;

    String message() default "";

}
