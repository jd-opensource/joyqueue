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
package org.joyqueue.nsr;


import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.nsr.admin.AppAdmin;
import org.joyqueue.nsr.admin.BrokerAdmin;
import org.joyqueue.nsr.admin.TopicAdmin;

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
