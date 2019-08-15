package io.chubao.joyqueue.handler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generic value binder interface
 * Created by chenyanying3 on 19-3-13.
 * Since jdk 1.8
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericValue {

    /**
     * 键
     *
     * @return 键
     */
    String value() default "";

    /**
     * 是否可以为空
     *
     * @return 为空标识
     */
    boolean nullable() default false;

}
