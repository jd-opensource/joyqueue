package com.jd.journalq.springboot.starter;

import com.jd.journalq.handler.JMQRoutingVerticle;
import com.jd.laf.web.springboot.starter.SpringEnvironment;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpServerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unbrokendome.vertx.spring.EnableVertx;
import org.unbrokendome.vertx.spring.VerticleRegistrationBean;

import javax.annotation.Resource;

import static com.jd.laf.web.vertx.RoutingVerticle.DEFAULT_PORT;


/**
 * Created by yangyang115 on 18-9-27.
 */
@Configuration
@EnableConfigurationProperties(JMQVertxWebAutoConfiguration.VertxWebProperties.class)
@EnableVertx
public class JMQVertxWebAutoConfiguration {

    @Resource
    protected VertxWebProperties webProperties;

    @Bean
    @ConditionalOnMissingBean(JMQRoutingVerticle.class)
    public VerticleRegistrationBean routingVerticle(org.springframework.core.env.Environment environment, ApplicationContext context) {
        return new VerticleRegistrationBean(() -> new JMQRoutingVerticle(new SpringEnvironment(environment, context), webProperties.http), webProperties.routing);
    }

    @ConfigurationProperties(prefix = "vertx")
    public static class VertxWebProperties {
        @NestedConfigurationProperty
        protected DeploymentOptions routing = new DeploymentOptions();

        @NestedConfigurationProperty
        protected HttpServerOptions http = new HttpServerOptions().setPort(DEFAULT_PORT);

        public DeploymentOptions getRouting() {
            return routing;
        }

        public void setRouting(DeploymentOptions routing) {
            this.routing = routing;
        }

        public HttpServerOptions getHttp() {
            return http;
        }

        public void setHttp(HttpServerOptions http) {
            this.http = http;
        }
    }

}
