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
package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.domain.TopicUnsubscribedApplication;
import io.chubao.joyqueue.model.query.QApplication;
import io.chubao.joyqueue.model.QPageQuery;

import java.util.List;

/**
 * 应用服务
 * Created by yangyang115 on 18-7-27.
 */
public interface ApplicationService extends PageService<Application, QApplication> {

    /**
     * 查找应用
     *
     * @param code 应用代码
     * @return
     */
    Application findByCode(String code);

    /**
     * 查找应用
     *
     * @param codes 应用代码
     * @return
     */
    List<Application> findByCodes(List<String> codes);

    PageResult<TopicUnsubscribedApplication> findTopicUnsubscribedByQuery(QPageQuery<QApplication> query);

    PageResult<Application> findSubscribedByQuery(QPageQuery<QApplication> query);
}
