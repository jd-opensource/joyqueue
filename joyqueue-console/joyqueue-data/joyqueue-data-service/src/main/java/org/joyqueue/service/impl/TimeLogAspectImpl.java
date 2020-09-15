/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.service.impl;


import com.alibaba.fastjson.JSON;
import org.joyqueue.toolkit.time.SystemClock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimeLogAspectImpl {
    private static final Logger logger= LoggerFactory.getLogger(TimeLogAspectImpl.class);
//    @Around("execution(* org.joyqueue.service.BrokerMonitorService.*(..))&&execution(* org.joyqueue.nsr.*.*(..))")

    @Around("execution(* org.joyqueue.service.BrokerMonitorService.*(..))")
    public Object around(ProceedingJoinPoint joinPoint){
       StringBuilder log=new StringBuilder();
       Object[] args= joinPoint.getArgs();
       String className= joinPoint.getSignature().getDeclaringType().getSimpleName();
       String methodName=joinPoint.getSignature().getName();
       log.append(className).append(":").append(methodName).append(";");
       long startTimeMs= SystemClock.now();
       try {
           Object result=  joinPoint.proceed();
           log.append("time elapsed:");
           log.append(SystemClock.now()-startTimeMs);
           log.append(" ms;");
           for(Object o:args){
               log.append(JSON.toJSONString(o)).append(";");
           }
           logger.info(log.toString());
           return result;
       }catch (Throwable throwable){
           logger.info(throwable.getMessage());
       }
       return null;
    }



}
