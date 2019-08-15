package io.chubao.joyqueue.model.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnumType {

    /**
     * 返回枚举类型
     *
     * @return 枚举类型
     */
    Class<? extends EnumItem> value();
}