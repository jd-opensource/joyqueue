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
package org.joyqueue.springboot.starter.condition;


import org.joyqueue.springboot.starter.properties.MsgQueryConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author jiangnan53
 * @date 2020/4/21
 **/
public class MsgQueryEnabledCondition extends SpringBootCondition {

    private static final Logger logger = LoggerFactory.getLogger(MsgQueryEnabledCondition.class);

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MsgQueryConfigurationProperties msgQueryConfigurationProperties=getMsgQueryProperties(context);
        if(!msgQueryConfigurationProperties.getEnabled()){
            logger.warn("message query function is disabled, because [message.query.enabled = false]");
            return ConditionOutcome.noMatch("message query function is disabled, because [message.query.enabled = false]");
        }
        logger.info("message query function is enabled");
        return ConditionOutcome.match();
    }

    private MsgQueryConfigurationProperties getMsgQueryProperties(ConditionContext context){
        MsgQueryConfigurationProperties msgQueryConfigurationProperties=new MsgQueryConfigurationProperties(context.getEnvironment());
        Binder.get(context.getEnvironment()).bind("message.query", Bindable.ofInstance(msgQueryConfigurationProperties));
        return msgQueryConfigurationProperties;
    }
}
