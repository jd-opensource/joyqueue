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
package org.joyqueue.handler.routing.command.topic;


import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.model.query.QTopicPartitionGroup;
import org.joyqueue.service.TopicPartitionGroupService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Topic partition group command
 * Created by chenyanying3 on 2018-10-19
 */
public class TopicPartitionGroupCommand extends NsrCommandSupport<TopicPartitionGroup, TopicPartitionGroupService, QTopicPartitionGroup> {

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QTopicPartitionGroup> qPageQuery) throws Exception {
        QTopicPartitionGroup query = qPageQuery.getQuery();
        List<TopicPartitionGroup> topicPartitionGroups = Collections.emptyList();

        if (query.getTopic() != null) {
            topicPartitionGroups = service.findByTopic(query.getNamespace(), query.getTopic());
        }
        topicPartitionGroups.sort(Comparator.comparingInt(TopicPartitionGroup::getGroupNo));
        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(topicPartitionGroups.size());

        PageResult<TopicPartitionGroup> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(topicPartitionGroups);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("findByTopic")
    public Response findByTopic(@Body Subscribe subscribe) throws Exception {
        return Responses.success(service.findByTopic(subscribe.getNamespace(), subscribe.getTopic()));
    }

    @Path("addPartition")
    public Response addPartition(@Body TopicPartitionGroup topicPartitionGroup) throws Exception {
        return Responses.success(service.addPartition(topicPartitionGroup));
    }

    @Path("removePartition")
    public Response removePartition(@Body TopicPartitionGroup topicPartitionGroup) throws Exception {
        return Responses.success(service.removePartition(topicPartitionGroup));
    }

    @Path("delete")
    public Response delete(@Body TopicPartitionGroup model) throws Exception {
        TopicPartitionGroup newModel = service.findById(model.getId());
        if (newModel == null) {
            throw new ConfigException(deleteErrorCode());
        }
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }

}
