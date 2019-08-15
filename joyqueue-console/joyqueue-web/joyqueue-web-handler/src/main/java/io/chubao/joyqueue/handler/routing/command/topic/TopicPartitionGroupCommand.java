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
package io.chubao.joyqueue.handler.routing.command.topic;


import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.domain.Subscribe;
import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QTopicPartitionGroup;
import io.chubao.joyqueue.service.TopicPartitionGroupService;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * Topic partition group command
 * Created by chenyanying3 on 2018-10-19
 */
public class TopicPartitionGroupCommand extends NsrCommandSupport<TopicPartitionGroup, TopicPartitionGroupService, QTopicPartitionGroup> {

    @Path("findByTopic")
    public Response findByTopic(@Body Subscribe subscribe) throws Exception {
        return Responses.success(service.findByTopic( subscribe.getNamespace(),subscribe.getTopic()));
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
