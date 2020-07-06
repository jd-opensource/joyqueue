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
 * 用户认证
 */
public interface Authentication {

    /**
     * 返回用户信息
     *
     * @param user 用户
     * @throws AuthException
     */
    UserDetails getUser(String user) throws AuthException;

    /**
     * 获取加密器
     *
     * @return 加密器
     */
    PasswordEncoder getPasswordEncode();

}