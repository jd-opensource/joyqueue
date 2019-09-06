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
package io.chubao.joyqueue.convert;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.PartitionGroupReplica;
import io.chubao.joyqueue.model.domain.Topic;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public class NsrReplicaConverter extends Converter<PartitionGroupReplica,Replica> {
    // TODO brokerId
    @Override
    protected Replica forward(PartitionGroupReplica partitionGroupReplica) {
        Replica replica = new Replica();
        replica.setId(partitionGroupReplica.getId());
        replica.setBrokerId(partitionGroupReplica.getBrokerId());
        replica.setGroup(partitionGroupReplica.getGroupNo());
        replica.setTopic(new TopicName(partitionGroupReplica.getTopic().getCode(),partitionGroupReplica.getNamespace().getCode()));
        return replica;
    }

    @Override
    protected PartitionGroupReplica backward(Replica replica) {
        PartitionGroupReplica  partitionGroupReplica = new PartitionGroupReplica();
        partitionGroupReplica.setId(replica.getId());
        partitionGroupReplica.setBrokerId(replica.getBrokerId());
        partitionGroupReplica.setGroupNo(replica.getGroup());
        partitionGroupReplica.setNamespace(new Namespace(replica.getTopic().getNamespace()));
        partitionGroupReplica.setTopic(new Topic(replica.getTopic().getCode()));
        return partitionGroupReplica;
    }
}
