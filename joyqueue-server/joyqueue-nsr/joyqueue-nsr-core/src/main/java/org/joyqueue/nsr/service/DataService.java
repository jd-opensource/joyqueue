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
package org.joyqueue.nsr.service;


import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.Query;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/11/8
 */
@Deprecated
public interface DataService<T, Q extends Query, K> {
    /**
     * 根据ID获取
     *
     * @param id
     * @return
     */
    T getById(K id);

    /**
     * 根据model获取
     *
     * @param model
     * @return
     */
    T get(T model);

    /**
     * 添加或者更新
     *
     * @param t
     */
    void addOrUpdate(T t);

    /**
     * 根据ID删除
     *
     * @param id
     */
    void deleteById(K id);

    /**
     * 根据model删除
     *
     * @param model
     */
    void delete(T model);

    /**
     * 获取所有
     *
     * @return
     */
    List<T> list();

    /**
     * 获取所有
     *
     * @return
     */
    List<T> list(Q query);

    /**
     * 分页查询
     *
     * @param pageQuery
     * @return
     */
    PageResult<T> pageQuery(QPageQuery<Q> pageQuery);
}
