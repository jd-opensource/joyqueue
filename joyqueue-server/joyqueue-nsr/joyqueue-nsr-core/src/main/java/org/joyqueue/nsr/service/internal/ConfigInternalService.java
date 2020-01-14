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
package org.joyqueue.nsr.service.internal;

import org.joyqueue.domain.Config;

import java.util.List;


/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public interface ConfigInternalService {

    /**
     * 根据id
     * @param id
     * @return
     */
    Config getById(String id);

    /**
     * 根据分组和Key获取配置
     *
     * @param group
     * @param key
     * @return
     */
    Config getByGroupAndKey(String group, String key);

    /**
     * 获取全部
     * @return
     */
    List<Config> getAll();

    /**
     * 添加配置
     *
     * @param config
     */
    Config add(Config config);

    /**
     * 更新配置
     *
     * @param config
     */

    Config update(Config config);

    /**
     * 删除配置
     *
     * @param id
     */
    void delete(String id);
}
