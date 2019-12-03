/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for the access-point element.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class AttributeBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String ELEMENT_KEY = "key";
    private static final String ELEMENT_VALUE = "value";

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.addPropertyValue(ELEMENT_KEY, element.getAttribute(ELEMENT_KEY));
        builder.addPropertyValue(ELEMENT_VALUE, element.getAttribute(ELEMENT_VALUE));
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return KeyValueAttribute.class;
    }
}
