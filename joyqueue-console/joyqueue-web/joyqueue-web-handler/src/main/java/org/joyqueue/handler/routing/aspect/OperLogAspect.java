/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.joyqueue.handler.routing.aspect;

import com.alibaba.fastjson.JSON;
import com.jd.laf.web.vertx.annotation.Path;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.joyqueue.context.GlobalApplicationContext;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.handler.routing.command.application.ApplicationTokenCommand;
import org.joyqueue.handler.routing.command.broker.BrokerCommand;
import org.joyqueue.handler.routing.command.config.ConfigCommand;
import org.joyqueue.handler.routing.command.config.DataCenterCommand;
import org.joyqueue.handler.routing.command.monitor.ConsumerCommand;
import org.joyqueue.handler.routing.command.monitor.ProducerCommand;
import org.joyqueue.handler.routing.command.topic.NamespaceCommand;
import org.joyqueue.handler.routing.command.topic.PartitionGroupReplicaCommand;
import org.joyqueue.handler.routing.command.topic.TopicCommand;
import org.joyqueue.handler.routing.command.topic.TopicPartitionGroupCommand;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.nsr.NsrServiceProvider;
import com.jd.laf.web.vertx.response.Response;
import org.joyqueue.service.OperLogService;
import org.joyqueue.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * @author jiangnan53
 * @date 2020/6/2
 **/
@Aspect
public class OperLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperLogAspect.class);

    private final Set<Class<?>> exceptCommandClasses = new HashSet<>(
            Arrays.asList(TopicCommand.class, NamespaceCommand.class, ConsumerCommand.class,
                    ProducerCommand.class, ApplicationTokenCommand.class, BrokerCommand.class,
                    TopicPartitionGroupCommand.class, PartitionGroupReplicaCommand.class,
                    ConfigCommand.class, DataCenterCommand.class, NsrCommandSupport.class)
    );

    @Around("@annotation(com.jd.laf.web.vertx.annotation.Path)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result instanceof Response) {
            Response response = (Response) result;
            Class<?> clazz = joinPoint.getSignature().getDeclaringType();
            if (response.getCode() == 200 && !exceptCommandClasses.contains(clazz)) {
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                Path path = methodSignature.getMethod().getAnnotation(Path.class);
                int operType = -1;
                if (StringUtils.containsIgnoreCase(path.value(), "add")) {
                    operType = OperLog.OperType.ADD.value();
                } else if (StringUtils.containsIgnoreCase(path.value(), "delete")) {
                    operType = OperLog.OperType.DELETE.value();
                } else if (StringUtils.containsIgnoreCase(path.value(), "update")) {
                    operType = OperLog.OperType.UPDATE.value();
                }
                if (operType >= 1 && operType <= 3) {
                    addOperLog(clazz.getSimpleName(), path.value(), joinPoint.getArgs(), operType);
                }
            }
        }
        return result;
    }

    private void addOperLog(String className, String pathValue, Object[] args, int operType) {
        OperLog operLog = null;
        StringBuilder target = new StringBuilder();
        //组装数据
        try {
            //记录操作日志
            operLog = new OperLog();
            operLog.setType(0);
            operLog.setOperType(operType);
            NsrServiceProvider nsrServiceProvider = GlobalApplicationContext.getBean(NsrServiceProvider.class);
            target.append(nsrServiceProvider.getBaseUrl()).append(",")
                    .append("class#").append(className).append(",")
                    .append("path#").append(pathValue).append(",")
                    .append(JSON.toJSONString(args));
        } catch (Exception e) {
            target.append(",").append(e.getMessage());
            logger.error("post exception", e);
            throw new RuntimeException("post exception", e);
        } finally {
            //执行记录日志
            try {
                //最长200
                String targetStr = target.toString();
                if (targetStr.length() > 500) {
                    targetStr = targetStr.substring(0, 490);
                }

                if (LocalSession.getSession() != null && LocalSession.getSession().getUser() != null) {
                    Long id = LocalSession.getSession().getUser().getId();
                    operLog.setUpdateBy(new Identity(id));
                    operLog.setCreateBy(new Identity(id));
                } else {
                    operLog.setUpdateBy(new Identity(0L));
                    operLog.setCreateBy(new Identity(0L));
                }
                operLog.setCreateTime(new Date());
                operLog.setIdentity(className.replace("Command", ""));
                operLog.setTarget(targetStr);
                OperLogService operLogService = GlobalApplicationContext.getBean(OperLogService.class);
                operLogService.add(operLog);
            } catch (Exception e) {
                logger.error("operLogService add", e);
            }
        }
    }

}
