package com.jd.journalq.handler.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 泛型Body
 * Created by chenyanying3 on 2018-10-17.
 * Since jdk 1.8
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericFieldBody {

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
