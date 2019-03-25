package com.jd.journalq.springboot.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by yangyang115 on 18-9-27.
 */
@Configuration
@ComponentScan(value = {
        "com.jd.journalq.nsr.impl",
        "com.jd.journalq.service.impl",
        "com.jd.journalq.async",
        "com.jd.journalq.other",})
@MapperScan(basePackages = {"com.jd.journalq.repository"})
@EnableTransactionManagement
public class ServiceAutoConfiguration {

}
