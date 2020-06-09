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
package org.joyqueue.handler.routing.command.config;


import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.DataCenter;
import org.joyqueue.model.query.QDataCenter;
import org.joyqueue.service.DataCenterService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2018/10/19.
 */
public class DataCenterCommand extends NsrCommandSupport<DataCenter,DataCenterService,QDataCenter> {

    private static final String group = "dataCenter";

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QDataCenter> qPageQuery) throws Exception {
        List<DataCenter> dataCenters = service.findAllDataCenter();
        List<DataCenter> dataCenterList;

        QDataCenter qDataCenter = qPageQuery.getQuery();
        if(StringUtils.isNotBlank(qDataCenter.getCode())) {
            dataCenterList = dataCenters.stream().filter(dataCenter -> StringUtils.containsIgnoreCase(dataCenter.getCode(), qDataCenter.getCode())).collect(Collectors.toList());
        } else {
            dataCenterList = new ArrayList<>(dataCenters);
        }

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(dataCenterList.size());

        PageResult<DataCenter> result = new PageResult<>();
        result.setPagination(pagination);
        result.setResult(dataCenterList);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("findAll")
    public Response findAll() throws Exception {
        return Responses.success(service.findAllDataCenter());
    }

    @Override
    @Path("delete")
    public Response delete(@QueryParam(Constants.ID) String id) throws Exception {
        DataCenter newModel = service.findById(id);
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        return Responses.success();
    }

    @Path("findByIps")
    public Response findByIps(@Body List<String> ips) throws Exception {
        List<DataCenter> dataCenters = service.findByIps(ips);
        return Responses.success(dataCenters);
    }

}
