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
package org.joyqueue.handler.routing.command.chart;

import org.joyqueue.handler.Constants;
import org.joyqueue.handler.routing.command.CommandSupport;
import org.joyqueue.model.ListQuery;
import org.joyqueue.exception.ValidationException;
import org.joyqueue.model.domain.Metric;
import org.joyqueue.model.query.QMetric;
import org.joyqueue.service.MetricService;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import static org.joyqueue.exception.ValidationException.NOT_FOUND_EXCEPTION_STATUS;
import static org.joyqueue.exception.ValidationException.UNIQUE_EXCEPTION_STATUS;

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
    public Response update(@QueryParam(Constants.ID) Long id, @Body Metric metric) throws Exception {
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

    @Path("findByPermission")
    public Response findByPermission(@QueryParam("userPermission") Boolean userPermission) throws Exception {
        return Responses.success(service.findByQuery(new ListQuery<>(new QMetric(userPermission))));
    }
}
