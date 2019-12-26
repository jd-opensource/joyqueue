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

import com.alibaba.fastjson.JSON;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * HandlerInvoker
 *
 * author: gaohaoxiang
 * date: 2018/10/16
 */
public class HandlerInvoker {

    private Object service;
    private String methodName;
    private Map<String, Class<?>> params;

    private Method method;

    public HandlerInvoker(Object service, String methodName, Map<String, Class<?>> params) {
        this.service = service;
        this.methodName = methodName;
        this.params = params;
        this.method = getMethod(service, methodName);
    }

    /**
     * 没处理重载方法
     *
     * @return
     */
    protected Method getMethod(Object service, String methodName) {
        for (Method method : service.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException(String.format("method not exist, method: %s.%s", service.getClass().getSimpleName(), methodName));
    }

    public Object invoke(RoutingContext context) throws Exception {
        if (params.isEmpty()) {
            return method.invoke(service);
        } else {
            Object[] args = new Object[params.size()];
            int index = 0;
            for (Map.Entry<String, Class<?>> entry : params.entrySet()) {
                String valueStr = context.request().getParam(entry.getKey());
                Object value = convertParam(entry.getKey(), entry.getValue(), valueStr);
                args[index] = value;
                index++;
            }
            return method.invoke(service, args);
        }
    }

    protected Object convertParam(String name, Class<?> type, String value) {
        if (type.getName().startsWith("java.lang")) {
            return ConvertUtils.convert(value, type);
        } else {
            return JSON.parseObject(value, type);
        }
    }
}