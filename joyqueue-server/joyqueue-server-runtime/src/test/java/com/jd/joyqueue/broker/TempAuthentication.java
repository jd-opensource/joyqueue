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
package com.jd.joyqueue.broker;

import com.jd.joyqueue.response.BooleanResponse;
import com.jd.joyqueue.security.Authentication;
import com.jd.joyqueue.security.PasswordEncoder;
import com.jd.joyqueue.security.UserDetails;
import com.jd.joyqueue.toolkit.security.auth.AuthException;

/**
 * Created by chengzhiliang on 2018/9/27.
 */
public class TempAuthentication implements Authentication {

    public static final String USERNAME = "admin";

    public static final String PASSWORD = "admin";

    @Override
    public BooleanResponse auth(String userName, String password) {
        return null;
    }

    @Override
    public BooleanResponse auth(String userName, String password, boolean checkAdmin) {
        return null;
    }

    @Override
    public boolean isAdmin(String userName) {
        return false;
    }

    public class TempUserDetails implements UserDetails {

        @Override
        public String getUsername() {
            return USERNAME;
        }

        @Override
        public String getPassword() {
            return PASSWORD;
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
            return true;
        }
    }

    public class TempPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(String password) throws AuthException {
            return password;
        }
    }
}