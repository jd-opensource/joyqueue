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

import org.joyqueue.model.domain.BaseModel;

/**
 * 服务接口
 * Created by yangyang115 on 18-7-26.
 */
public interface Service<M extends BaseModel> {
    /**
     * 根据ID查找
     *
     * @param id
     * @return
     */
    M findById(long id);

    /**
     * 增加
     *
     * @param model
     * @return
     */
    int add(M model);

    /**
     * 修改
     *
     * @param  model：必须有id信息
     * @return
     */
    int update(M model);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    int deleteById(long id);

    /**
     * 删除
     *
     * @param model
     * @return
     */
    int delete(M model);

    /**
     * 更新状态
     * @param model：必须有id，status，updateBy, updateTime信息
     * @return
     */
    int updateStatus(M model);

}
