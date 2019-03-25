package com.jd.journalq.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.net.URL;

/**
 * Created by bjliuyong on 2018/11/30.
 */
@SpringBootApplication
@PropertySource({"classpath:important.properties"})
public class ApiApplication {

    public static void main(String[] args) {
        //设置日志
        URL resource = Thread.currentThread().getContextClassLoader().getResource("logging.properties");
        if (resource != null) {
            String path = resource.getFile();
            if (path != null) {
                System.setProperty("java.util.logging.config.file", path);
            }
        }
        System.setProperty("vertx.logger-delegate-factory-class-name","io.vertx.core.logging.SLF4JLogDelegateFactory");
        SpringApplication.run(ApiApplication.class, args);
    }
}
