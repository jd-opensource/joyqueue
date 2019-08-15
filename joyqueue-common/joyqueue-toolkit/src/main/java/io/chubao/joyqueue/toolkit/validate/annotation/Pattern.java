package io.chubao.joyqueue.toolkit.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 不能为空验证
 * Created by hexiaofeng on 15-7-20.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Pattern {

    /**
     * 正则表达式
     *
     * @return 正则表达式
     */
    String regex() default "";

    /**
     * 是否要求满足正则表达式
     *
     * @return
     */
    boolean flag() default true;

    /**
     * 消息提示
     *
     * @return
     */
    String message() default "";

}
