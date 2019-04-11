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
package com.jd.journalq.service.impl;

import com.jd.journalq.model.QKeyword;
import com.jd.journalq.model.domain.UserToken;
import com.jd.journalq.repository.UserTokenRepository;
import com.jd.journalq.service.UserTokenService;
import org.springframework.stereotype.Service;

@Service("userTokenService")
public class UserTokenServiceImpl extends PageServiceSupport<UserToken, QKeyword, UserTokenRepository> implements UserTokenService {

    @Override
    public UserToken findByCode(String code) {
        return repository.findByCode(code);
    }
}
