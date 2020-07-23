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
package org.joyqueue.springboot.starter;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.joyqueue.service.TopicMsgFilterService;
import org.joyqueue.springboot.starter.condition.MsgQueryEnabledCondition;
import org.joyqueue.springboot.starter.properties.MsgQueryConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangnan53
 * @date 2020/4/21
 **/
@Configuration
@Conditional(MsgQueryEnabledCondition.class)
@EnableConfigurationProperties(MsgQueryConfigurationProperties.class)
public class MsgQueryConfiguration implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MsgQueryConfiguration.class);

    private final ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1,new ThreadFactoryBuilder().setNameFormat("msg-query-%d").setDaemon(true).build());


    @Autowired
    private TopicMsgFilterService topicMsgFilterService;

    @Override
    public void destroy()  {
        threadPoolExecutor.shutdown();
    }

    @Override
    public void afterPropertiesSet()  {
        threadPoolExecutor.scheduleAtFixedRate(()->{
            try {
                topicMsgFilterService.execute();
            } catch (Exception e) {
                if (e instanceof IllegalAccessException) {
                    logger.warn(e.getMessage());
                } else {
                    logger.error("Cause error", e);
                }
            }
        },60,10,TimeUnit.SECONDS);
    }
}
