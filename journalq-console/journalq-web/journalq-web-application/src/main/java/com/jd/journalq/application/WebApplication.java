package com.jd.journalq.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.net.URL;

/**
 * Created by chenyanying3 on 19-3-3.
 */
@SpringBootApplication
@PropertySource({"classpath:important.properties"})
public class WebApplication {
    public static void main(String[] args) {
        //设置日志
        URL resource = WebApplication.class.getClassLoader().getResource("logging.properties");
        if (resource != null) {
            String path = resource.getFile();
            if (path != null) {
                System.setProperty("java.util.logging.config.file", path);
            }
        }
        System.setProperty("vertx.logger-delegate-factory-class-name","io.vertx.core.logging.SLF4JLogDelegateFactory");
        
        SpringApplication.run(WebApplication.class, args);
    }

}
