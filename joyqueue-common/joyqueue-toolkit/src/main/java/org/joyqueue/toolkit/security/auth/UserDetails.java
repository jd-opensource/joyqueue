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
package org.joyqueue.toolkit.security.auth;

/**
 * 用户接口
 */
public interface UserDetails {

    /**
     * 获取用户名称
     *
     * @return 用户名称
     */
    String getUsername();

    /**
     * 获取用户密码
     *
     * @return 密码
     */
    String getPassword();

    /**
     * 是否过期
     *
     * @return 过期表示
     */
    boolean isExpired();

    /**
     * 是否锁定
     *
     * @return 锁定标示
     */
    boolean isLocked();

    /**
     * 是否启用
     *
     * @return 启用标示
     */
    boolean isEnabled();

    /**
     * 是否是管理员用户
     *
     * @return 管理员标示
     */
    boolean isAdmin();

}