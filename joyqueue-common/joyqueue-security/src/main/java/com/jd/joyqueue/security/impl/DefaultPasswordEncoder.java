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
package com.jd.joyqueue.security.impl;

import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.exception.JournalqException;
import com.jd.joyqueue.security.PasswordEncoder;
import com.jd.joyqueue.toolkit.security.Encrypt;
import com.jd.joyqueue.toolkit.security.Sha;

/**
 * @author majun8
 */
public class DefaultPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(String password) throws JournalqException {
        try {
            return Encrypt.encrypt(password, Encrypt.DEFAULT_KEY, Sha.INSTANCE);
        } catch (Exception e) {
            throw new JournalqException(JournalqCode.CN_AUTHENTICATION_ERROR);
        }

    }
}
