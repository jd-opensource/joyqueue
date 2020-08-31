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
package org.joyqueue.broker.manage.exporter.vertx;

import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.monitor.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ConvertInvoker
 *
 * author: gaohaoxiang
 * date: 2018/12/21
 */
public class ConvertInvoker extends HandlerInvoker {
    /**
     * 重试扩展点
     */
    ExtensionPoint<Converter, String> converters = new ExtensionPointLazy<>(Converter.class, SpiLoader.INSTANCE, null, null);

    private static final String CONVERTER_PARAM = "_target";

    private static final String CONVERTER_HEADER = "User-Agent";

    protected static final Logger logger = LoggerFactory.getLogger(ConvertInvoker.class);

    public ConvertInvoker(Object service, String methodName, Map<String, Class<?>> params) {
        super(service, methodName, params);
    }

    @Override
    public Object invoke(RoutingContext context) throws Exception {
        String target = getTarget(context);
        Object result = super.invoke(context);

        if (StringUtils.isBlank(target) || result == null) {
            return result;
        }

        Converter converter = null;
        for (Converter extension : converters.extensions()) {
            if (target.startsWith(String.valueOf(extension.type()))) {
                converter = extension;
                break;
            }
        }
        if (converter == null) {
            return result;
        }
        return converter.convert(result);
    }

    protected String getTarget(RoutingContext context) {
        String targetParam = context.request().getParam(CONVERTER_PARAM);
        if (StringUtils.isNotBlank(targetParam)) {
            return targetParam;
        }
        return context.request().getHeader(CONVERTER_HEADER);
    }
}