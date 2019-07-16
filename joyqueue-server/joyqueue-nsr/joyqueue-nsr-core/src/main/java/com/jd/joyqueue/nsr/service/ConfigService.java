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
package com.jd.joyqueue.nsr.service;

import com.jd.joyqueue.domain.Config;
import com.jd.joyqueue.nsr.model.ConfigQuery;


/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public interface ConfigService extends DataService<Config, ConfigQuery, String> {

    /**
     * 根据分组和Key获取配置
     *
     * @param group
     * @param key
     * @return
     */
    Config getByGroupAndKey(String group, String key);

    /**
     * 添加配置
     *
     * @param config
     */
    void add(Config config);

    /**
     * 更新配置
     *
     * @param config
     */

    void update(Config config);

    /**
     * 删除配置
     *
     * @param config
     */
    void remove(Config config);
}
