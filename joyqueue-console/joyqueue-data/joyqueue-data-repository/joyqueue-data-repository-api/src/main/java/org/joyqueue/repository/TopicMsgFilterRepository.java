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
package org.joyqueue.repository;

import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.TopicMsgFilter;
import org.joyqueue.model.query.QTopicMsgFilter;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
@Repository
public interface TopicMsgFilterRepository extends PageRepository<TopicMsgFilter, QTopicMsgFilter> {

    /**
     * 从数据库中获取该用户的下一个任务
     * @return
     */
    List<TopicMsgFilter> findByNextOne(int size);


    PageResult<TopicMsgFilter> findTopicMsgFiltersByQuery(QPageQuery<QTopicMsgFilter> query);

}
