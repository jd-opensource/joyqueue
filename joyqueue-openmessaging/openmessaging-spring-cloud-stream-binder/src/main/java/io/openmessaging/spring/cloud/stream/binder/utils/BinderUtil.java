/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.spring.cloud.stream.binder.utils;

import io.openmessaging.spring.boot.config.OMSProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSBinderConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Binder Util
 */
public final class BinderUtil {

    private BinderUtil() {

    }

    public static OMSProperties mergeProperties(OMSBinderConfigurationProperties omsBinderConfigurationProperties,
                                                OMSProperties omsProperties) {
        OMSProperties result = new OMSProperties();
        if (StringUtils.isEmpty(omsProperties.getUrl())) {
            result.setUrl(omsBinderConfigurationProperties.getUrl());
        } else {
            result.setUrl(omsProperties.getUrl());
        }
        if (null == omsProperties.getAttributes() || omsProperties.getAttributes().isEmpty()) {
            result.setAttributes(omsBinderConfigurationProperties.getAttributes());
        } else {
            result.setAttributes(omsProperties.getAttributes());
        }
        return result;
    }

}
