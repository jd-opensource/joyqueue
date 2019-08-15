package io.chubao.joyqueue.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chenyanying3 on 19-4-13.
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.driver", havingValue = "h2")
public class H2DBServerAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(H2DBServerAutoConfiguration.class);

    @Bean(value = "h2DBServer", initMethod = "init", destroyMethod = "stop")
    public H2DBServer runH2DBServer() {
        return new H2DBServer();
    }

}
