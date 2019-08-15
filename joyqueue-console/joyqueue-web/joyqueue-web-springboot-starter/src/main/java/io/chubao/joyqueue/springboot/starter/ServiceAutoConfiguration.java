package io.chubao.joyqueue.springboot.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Service auto configuration
 * Created by chenyanying3 on 18-11-16.
 */
@Configuration
@ComponentScan(value = {
        "io.chubao.joyqueue.nsr.impl",
        "io.chubao.joyqueue.service.impl",
        "io.chubao.joyqueue.async",
        "io.chubao.joyqueue.other",})
@MapperScan(basePackages = {"io.chubao.joyqueue.repository"})
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class ServiceAutoConfiguration {

}
