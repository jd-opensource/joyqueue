package io.openmessaging.spring.boot.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a TransactionStateCheckListener.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface OMSTransactionStateCheckListener {

    @AliasFor(annotation = Component.class)
    String value() default "";
}