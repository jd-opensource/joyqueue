package com.jd.journalq.handler.binder.annotation;

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
public @interface GenericBody {

    int typeindex();
    /**
     * 数据类型
     *
     * @return
     */
    BodyType type() default BodyType.DETECT;

    /**
     * 数据类型
     */
    enum BodyType {
        /**
         * 自动检查
         */
        DETECT,
        /**
         * 普通文本
         */
        TEXT,
        /**
         * JSON格式
         */
        JSON,
        /**
         * XML格式
         */
        XML,
        /**
         * PEOPERTIES格式
         */
        PROPERTIES

    }
}
