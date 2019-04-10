package com.jd.journalq.springboot.starter;

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
        "com.jd.journalq.nsr.impl",
        "com.jd.journalq.service.impl",
        "com.jd.journalq.async",
        "com.jd.journalq.other",})
@MapperScan(basePackages = {"com.jd.journalq.repository"})
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class ServiceAutoConfiguration {

}
