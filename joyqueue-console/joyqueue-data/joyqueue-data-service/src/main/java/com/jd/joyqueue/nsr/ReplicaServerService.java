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
package com.jd.joyqueue.nsr;

import com.jd.joyqueue.model.domain.PartitionGroupReplica;
import com.jd.joyqueue.model.query.QPartitionGroupReplica;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public interface ReplicaServerService extends NsrService<PartitionGroupReplica,QPartitionGroupReplica,String> {
    int deleteByGroup(String topic, int groupNo);
    List<PartitionGroupReplica> findByTopic(String topic);
    
    int deleteByTopic(String topic);
}
