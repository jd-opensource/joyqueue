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
package org.joyqueue.token;

import org.joyqueue.token.TokenSupplier;
import org.joyqueue.token.UuidTokenSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UUID令牌提供者自动配置
 */
@Configuration
@ConditionalOnProperty(prefix = "token", name = "type", havingValue = "uuid", matchIfMissing = true)
public class UuidTokenAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TokenSupplier.class)
    public TokenSupplier tokenSupplier() {
        return new UuidTokenSupplier();
    }

}
