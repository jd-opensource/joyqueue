package com.jd.journalq.token;

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
