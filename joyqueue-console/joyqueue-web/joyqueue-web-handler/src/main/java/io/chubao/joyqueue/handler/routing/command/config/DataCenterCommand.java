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
package io.chubao.joyqueue.handler.routing.command.config;


import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.DataCenter;
import io.chubao.joyqueue.model.query.QDataCenter;
import io.chubao.joyqueue.service.DataCenterService;

import java.util.List;

import static io.chubao.joyqueue.handler.Constants.ID;

/**
 * Created by wangxiaofei1 on 2018/10/19.
 */
public class DataCenterCommand extends NsrCommandSupport<DataCenter,DataCenterService,QDataCenter> {

    private static final String group = "dataCenter";

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QDataCenter> qPageQuery) throws Exception {
        List<DataCenter> dataCenters = service.findAllDataCenter();

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(dataCenters.size());

        PageResult<DataCenter> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(dataCenters);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("findAll")
    public Response findAll() throws Exception {
        return Responses.success(service.findAllDataCenter());
    }

    @Override
    @Path("delete")
    public Response delete(@QueryParam(ID) String id) throws Exception {
        DataCenter newModel = service.findById(id);
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        return Responses.success();
    }

}
