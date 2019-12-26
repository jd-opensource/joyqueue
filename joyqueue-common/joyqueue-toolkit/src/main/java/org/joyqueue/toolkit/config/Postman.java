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
package org.joyqueue.toolkit.config;

/**
 * 动态配置
 * Created by hexiaofeng on 16-8-29.
 */
public interface Postman {

    /**
     * 监听分组配置变更
     *
     * @param group    分组
     * @param listener 监听器
     */
    void addListener(String group, GroupListener listener);

    /**
     * 删除分组监听器
     *
     * @param group    分组
     * @param listener 监听器
     */
    void removeListener(String group, GroupListener listener);

    /**
     * 获取上下文
     *
     * @param group 分组
     * @return 上下文
     */
    Context get(String group);

    /**
     * 分组监听器
     */
    interface GroupListener {

        /**
         * 配置发生变更,每个监听器拿到的上下文是线程安全的
         *
         * @param group   分组
         * @param context 上下文
         */
        void onUpdate(String group, Context context);
    }

}
