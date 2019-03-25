package com.jd.journalq.handler.binder.annotation;

import com.jd.journalq.handler.binder.BodyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 泛型Body
 * @author wylixiaobin
 * Date: 2018/10/17
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {

    /**
     * parameter index
     **/
    int typeindex();
    /**
     * 数据类型
     *
     * @return
     */
    BodyType type() default BodyType.DETECT;

}
