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
package org.joyqueue.security.impl;

import org.joyqueue.security.UserDetails;

/**
 * @author majun8
 */
public class DefaultUserDetail implements UserDetails {

    private String user;
    private String password;
    private boolean admin;

    public DefaultUserDetail(String user, String password) {
        this(user, password, false);
    }

    public DefaultUserDetail(String user, String password, boolean admin) {
        this.user = user;
        this.password = password;
        this.admin = admin;
    }

    @Override
    public String getUsername() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }
}
