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
package org.joyqueue.service;

import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.TopicMsgFilter;
import org.joyqueue.model.query.QTopicMsgFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
public interface TopicMsgFilterService extends PageService<TopicMsgFilter, QTopicMsgFilter> {

    Logger logger = LoggerFactory.getLogger(TopicMsgFilterService.class);

    /**
     * 创建异步线程后台消费{@param filter.getTopic()}
     * @throws IOException
     */
    void execute() throws Exception;



    PageResult<TopicMsgFilter> findTopicMsgFilters(QPageQuery<QTopicMsgFilter> query);
}
