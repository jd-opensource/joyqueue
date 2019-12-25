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
package org.joyqueue.nsr.ignite.dao;

import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.Query;

import java.util.List;


public interface BaseDao<M, Q extends Query, K> {
    /**
     * 根据ID查找
     *
     * @param id
     * @return
     */
    M findById(K id);

    /**
     * 增加
     *
     * @param model
     * @return
     */
    void add(M model);

    /**
     * 修改
     *
     * @param model：必须有id信息
     * @return
     */
    void addOrUpdate(M model);


    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    void deleteById(K id);

    /**
     * 分页查找
     *
     * @param pageQuery
     * @return
     */
    PageResult<M> pageQuery(QPageQuery<Q> pageQuery);

    /**
     * 查找全部
     *
     * @return
     */
    List<M> list(Q query);
}
