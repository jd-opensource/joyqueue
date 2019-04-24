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
package com.jd.journalq.service;

import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.model.query.QPartitionGroupReplica;
import com.jd.journalq.nsr.NsrService;

/**
 * 主题Broker分组 服务
 * Created by chenyanying3 on 2018-10-18
 */
public interface PartitionGroupReplicaService extends NsrService<PartitionGroupReplica,QPartitionGroupReplica,String> {
    /**
     *扩容
     * @param replica
     * @param partitionGroup
     * @return
     */
    int addWithNameservice(PartitionGroupReplica replica, TopicPartitionGroup partitionGroup);

    /**
     * 锁容
     * @param replica
     * @param partitionGroup
     * @return
     */
    int removeWithNameservice(PartitionGroupReplica replica, TopicPartitionGroup partitionGroup);
}
