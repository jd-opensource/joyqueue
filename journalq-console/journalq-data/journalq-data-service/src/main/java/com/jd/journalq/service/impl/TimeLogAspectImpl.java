package com.jd.journalq.service.impl;


import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimeLogAspectImpl {
    private final static Logger logger= LoggerFactory.getLogger(TimeLogAspectImpl.class);
//    @Around("execution(* com.jd.journalq.service.BrokerMonitorService.*(..))&&execution(* com.jd.jmq.nsr.*.*(..))")

    @Around("execution(* com.jd.journalq.service.BrokerMonitorService.*(..))")
    public Object around(ProceedingJoinPoint joinPoint){
       StringBuilder log=new StringBuilder();
       Object[] args= joinPoint.getArgs();
       String className= joinPoint.getSignature().getDeclaringType().getSimpleName();
       String methodName=joinPoint.getSignature().getName();
       log.append(className).append(":").append(methodName).append(";");
       long startTimeMs=System.currentTimeMillis();
       try {
           Object result=  joinPoint.proceed();
           log.append("time elapsed:");
           log.append(System.currentTimeMillis()-startTimeMs);
           log.append(" ms;");
           for(Object o:args){
               log.append(JSON.toJSONString(o)).append(";");
           }
           logger.info(log.toString());
           return result;
       }catch (Throwable throwable){

       }
       return null;
    }



}
