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

import org.joyqueue.model.domain.Application;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.query.QApplication;
import org.joyqueue.model.QPageQuery;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 应用 仓库
 * Created by chenyanying3 on 2018-10-15
 */
@Repository
public interface ApplicationRepository extends PageRepository<Application, QApplication> {

    /**
     * 查询未订阅的app
     * @param query
     * @return
     */
    PageResult<Application> findUnsubscribedByQuery(QPageQuery<QApplication> query);

    /**
     * 根据代码查找
     *
     * @param applicationCode
     * @return
     */
    Application findByCode(String applicationCode);


    /**
     * 根据代码查找
     *
     * @param codes
     * @return
     */
    List<Application> findByCodes(List<String> codes);

//    /**
//     * 根据代码查找,检索出已删除应用
//     *
//     * @param applicationCode
//     * @return
//     */
//    List<Application> findWithDeletedByCode(String applicationCode);

}
