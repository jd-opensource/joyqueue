/**
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
package com.jd.journalq.nsr;


import com.jd.journalq.domain.Broker;
import com.jd.journalq.nsr.admin.AppAdmin;
import com.jd.journalq.nsr.admin.BrokerAdmin;
import com.jd.journalq.nsr.admin.TopicAdmin;

import java.io.Closeable;
import java.util.List;

public interface NsrAdmin extends Closeable {
    String publish(TopicAdmin.PubSubArg pubSubArg) throws Exception;
    String unPublish(TopicAdmin.PubSubArg pubSubArg) throws Exception;
    String subscribe(TopicAdmin.PubSubArg pubSubArg) throws Exception;
    String unSubscribe(TopicAdmin.PubSubArg pubSubArg) throws Exception;

    String createTopic(TopicAdmin.TopicArg topicArg) throws Exception;
    String delTopic(TopicAdmin.TopicArg topicArg) throws Exception;
    String token(AppAdmin.TokenArg tokenArg) throws Exception;
    String partitionGroup(TopicAdmin.PartitionGroupArg partitionGroupArg) throws Exception;
    List<Broker> listBroker(BrokerAdmin.ListArg listArg) throws Exception;

}
