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
package org.joyqueue.handler.routing.command.monitor;

import org.joyqueue.handler.annotation.Operator;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.Producer;
import org.joyqueue.model.domain.ProducerConfig;
import org.joyqueue.service.ProducerService;
import com.google.common.base.Preconditions;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import static com.jd.laf.web.vertx.annotation.Body.BodyType.JSON;

public class ProducerConfigAddOrUpdateCommand implements Command<Response>, Poolable {
    private final Logger logger = LoggerFactory.getLogger(ProducerConfigAddOrUpdateCommand.class);

    @Value(nullable = false)
    protected ProducerService producerService;

    @Body(type = JSON)
    @NotNull
    protected ProducerConfig producerConfig;
    @Operator
    protected Identity operator;

    @Override
    public String type() {
        return "addOrUpdateProducerConfig";
    }

    @Override
    public Response execute() throws Exception {
        Preconditions.checkArgument(null!=producerConfig,  "invalid argument");
        Producer producer = producerService.findById(producerConfig.getProducerId());
        producer.setConfig(producerConfig);
        return Responses.success(producerService.update(producer));
    }

    @Override
    public void clean() {
        producerConfig = null;
    }
}
