package io.openmessaging.spring.helper;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Utilities for processing {@link BeanDefinitionBuilder}.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class BeanDefinitionHelper {

    public static void addValue(BeanDefinitionBuilder builder, String field, Object value) {
        if (value == null) {
            return;
        }
        builder.addPropertyValue(field, value);
    }

    public static void addValue(BeanDefinitionBuilder builder, Element element, String field, String property) {
        String value = element.getAttribute(property);
        if (StringUtils.hasText(value)) {
            return;
        }
        builder.addPropertyValue(field, value);
    }

    public static void addValue(BeanDefinitionBuilder builder, Element element, String property) {
        addValue(builder, element, property, property);
    }
}