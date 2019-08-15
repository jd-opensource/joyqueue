package io.chubao.joyqueue.nsr;


import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.nsr.admin.AppAdmin;
import io.chubao.joyqueue.nsr.admin.BrokerAdmin;
import io.chubao.joyqueue.nsr.admin.TopicAdmin;

import java.io.Closeable;
import java.util.List;


public interface NsrAdmin extends Closeable {
    String publish(TopicAdmin.PublishArg pubSubArg) throws Exception;
    String unPublish(TopicAdmin.PublishArg pubSubArg) throws Exception;
    String subscribe(TopicAdmin.SubscribeArg pubSubArg) throws Exception;
    String unSubscribe(TopicAdmin.SubscribeArg pubSubArg) throws Exception;

    String createTopic(TopicAdmin.TopicArg topicArg) throws Exception;
    String delTopic(TopicAdmin.TopicArg topicArg) throws Exception;
    String token(AppAdmin.TokenArg tokenArg) throws Exception;
    List<AppToken> tokens(AppAdmin.TokensArg tokensArg) throws Exception;
    String partitionGroup(TopicAdmin.PartitionGroupArg partitionGroupArg) throws Exception;
    List<Broker> listBroker(BrokerAdmin.ListArg listArg) throws Exception;

}
