/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import io.openmessaging.KeyValue;
import io.openmessaging.OMS;
import io.openmessaging.spring.OMSSpringConsts;
import io.openmessaging.spring.support.AccessPointContainer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Parser for the access-point element.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class AccessPointBeanDefinitionParser implements BeanDefinitionParser {

    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_URL = "url";
    private static final String ELEMENT_ATTRIBUTE = "attribute";
    private static final String ELEMENT_ATTRIBUTE_KEY = "key";
    private static final String ELEMENT_ATTRIBUTE_VALUE = "value";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String id = element.getAttribute(ATTRIBUTE_ID);
        String url = element.getAttribute(ATTRIBUTE_URL);

        Assert.hasText(url, String.format("%s can not be blank", ATTRIBUTE_URL));

        if (!StringUtils.hasText(id)) {
            id = OMSSpringConsts.DEFAULT_ACCESS_POINT_ID;
        }

        KeyValue attributes = parseAttributes(element, parserContext);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(AccessPointContainer.class)
                .addConstructorArgValue(id)
                .addConstructorArgValue(url)
                .addConstructorArgValue(attributes);

        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        return beanDefinition;
    }

    protected KeyValue parseAttributes(Element element, ParserContext parserContext) {
        KeyValue attributes = OMS.newKeyValue();
        List<Element> attributeElements = DomUtils.getChildElementsByTagName(element, ELEMENT_ATTRIBUTE);

        for (Element attributeElement : attributeElements) {
            String key = attributeElement.getAttribute(ELEMENT_ATTRIBUTE_KEY);
            String value = attributeElement.getAttribute(ELEMENT_ATTRIBUTE_VALUE);
            attributes.put(key, value);
        }
        return attributes;
    }
}