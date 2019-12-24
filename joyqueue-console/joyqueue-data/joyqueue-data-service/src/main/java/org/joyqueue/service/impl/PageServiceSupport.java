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
package org.joyqueue.service.impl;

import org.joyqueue.model.domain.BaseModel;
import org.joyqueue.model.ListQuery;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.Query;
import org.joyqueue.repository.PageRepository;
import org.joyqueue.service.PageService;

import java.util.List;

/**
 * 业务域基础类(带分页)
 * Created by chenyanying3 on 2018-10-15.
 */
public abstract class PageServiceSupport<M extends BaseModel, Q extends Query, R extends PageRepository<M,Q>> extends ServiceSupport<M,R> implements PageService<M, Q> {
    /**
     * 根据分页条件查询，返回结果带分页
     *
     * @param query 分页查询
     * @return 匹配的实体列表，带分页
     */
    @Override
    public PageResult<M> findByQuery(final QPageQuery<Q> query) {
        if (query == null) {
            return PageResult.empty();
        }
        return repository.findByQuery(query);
    }

    /**
     * 根据条件查询
     *  @param query 查询条件
     ** @return 匹配的实体列表
     */
    @Override
    public List<M> findByQuery(ListQuery<Q> query){
        if (null == query || null == query.getQuery()) {
            throw new IllegalArgumentException("根据查询传参错误！");
        }
        return repository.findByQuery(query);
    }

}
