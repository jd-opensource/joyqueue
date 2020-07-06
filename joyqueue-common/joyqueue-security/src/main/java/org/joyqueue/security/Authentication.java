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
package org.joyqueue.security;

import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.response.BooleanResponse;

/**
 * @author majun8
 */
public interface Authentication {

    @Deprecated
    UserDetails getUser(String user) throws JoyQueueException;

    @Deprecated
    PasswordEncoder getPasswordEncode();

    BooleanResponse auth(String userName, String password);

    BooleanResponse auth(String userName, String password, boolean checkAdmin);

    boolean isAdmin(String userName);
}
