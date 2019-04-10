package com.jd.journalq.handler.routing.command.topic;


import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.model.domain.Subscribe;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.model.query.QTopicPartitionGroup;
import com.jd.journalq.service.TopicPartitionGroupService;
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
