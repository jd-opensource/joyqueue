package io.chubao.joyqueue.toolkit.config.annotation;

import io.chubao.joyqueue.toolkit.validate.annotation.DoubleRange;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 绑定上下文
 * Created by hexiaofeng on 15-7-20.
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleBinding {

    /**
     * 键
     *
     * @return 键
     */
    String key() default "";


    /**
     * 默认值
     *
     * @return 默认值
     */
    double defaultValue() default (double) 0;

    /**
     * 数值范围
     *
     * @return 数值范围
     */
    DoubleRange range() default @DoubleRange;

}
