package io.chubao.joyqueue.handler.routing.command.monitor;


import io.chubao.joyqueue.handler.annotation.Operator;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.domain.ProducerConfig;
import io.chubao.joyqueue.service.ProducerService;
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
