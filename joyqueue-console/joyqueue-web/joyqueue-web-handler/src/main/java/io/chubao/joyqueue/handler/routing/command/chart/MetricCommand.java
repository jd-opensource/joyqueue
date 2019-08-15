package io.chubao.joyqueue.handler.routing.command.chart;

import io.chubao.joyqueue.model.ListQuery;
import io.chubao.joyqueue.exception.ValidationException;
import io.chubao.joyqueue.handler.routing.command.CommandSupport;
import io.chubao.joyqueue.model.domain.Metric;
import io.chubao.joyqueue.model.query.QMetric;
import io.chubao.joyqueue.service.MetricService;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import static io.chubao.joyqueue.exception.ValidationException.NOT_FOUND_EXCEPTION_STATUS;
import static io.chubao.joyqueue.exception.ValidationException.UNIQUE_EXCEPTION_STATUS;
import static io.chubao.joyqueue.handler.Constants.ID;

/**
 * Created by libinghui3 on 2019/3/7.
 */
public class MetricCommand extends CommandSupport<Metric,MetricService,QMetric> {

    @Path("add")
    public Response add(@Body Metric metric) throws Exception {
        //validate metric code and alias code, unique
        if (service.findByCode(metric.getCode()) != null) {
            throw new ValidationException(UNIQUE_EXCEPTION_STATUS, "code|已经存在");
        }
        if (service.findByAliasCode(metric.getAliasCode()) != null) {
            throw new ValidationException(UNIQUE_EXCEPTION_STATUS, "aliasCode|已经存在");
        }
        //add
        return super.add(metric);
    }

    @Path("update")
    public Response update(@QueryParam(ID) Long id, @Body Metric metric) throws Exception {
        //validate metric code and alias code, unique
        if (service.findByCode(metric.getCode()) == null) {
            throw new ValidationException(NOT_FOUND_EXCEPTION_STATUS, "code|不存在");
        }
        //update
        return super.update(id, metric);
    }

    @Path("findAll")
    public Response findAll() throws Exception {
        return Responses.success(service.findByQuery(new ListQuery<>(new QMetric())));
    }
}
