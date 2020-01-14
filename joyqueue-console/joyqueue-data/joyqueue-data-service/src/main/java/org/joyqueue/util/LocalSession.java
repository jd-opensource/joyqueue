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
package org.joyqueue.util;

import org.joyqueue.model.domain.User;

/**
 * Created by wangxiaofei1 on 2018/12/12.
 */
public class LocalSession {
    private static final ThreadLocal<User> local = new ThreadLocal<User>();

    private static final LocalSession session =new  LocalSession();

    public User getUser() {
        return local.get();
    }

    public void setUser(User user) {
        local.set(user);
    }

    /**
     * 单例函数
     *
     * @return 会话
     */
    public static LocalSession getSession() {
        return session;
    }
}
