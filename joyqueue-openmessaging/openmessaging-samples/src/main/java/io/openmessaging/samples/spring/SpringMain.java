package io.openmessaging.samples.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringMain {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring1.xml");
    }
}