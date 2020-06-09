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

package org.joyqueue.context;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author jiangnan53
 * @date 2020/6/3
 **/
@Component
public class GlobalApplicationContext implements ApplicationContextAware {

    public static ApplicationContext APPLICATION_CONTEXT = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (APPLICATION_CONTEXT == null) {
            APPLICATION_CONTEXT = applicationContext;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return APPLICATION_CONTEXT.getBean(clazz);
    }
}
