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

import com.jd.journalq.model.domain.ApplicationUser;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.query.QUser;
import com.jd.journalq.repository.UserRepository;
import com.jd.journalq.service.UserService;
import com.jd.journalq.util.NullUtil;
import com.jd.journalq.util.ObjectUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangyang115 on 18-7-27.
 */
@Service("userService")
public class UserServiceImpl extends PageServiceSupport<User, QUser,UserRepository> implements UserService {

    @Override
    public User findByCode(final String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        return repository.findByCode(code);
    }

    @Override
   public List<User> findByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findByCodes(codes);
    }

    @Override
    public List<User> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findByIds(ids);
    }

    @Override
    public List<User> findByAppId(final long appId) {
        return repository.findByAppId(appId);
    }

    @Override
    public int addAppUser(final ApplicationUser appUser) {
        if (appUser == null) {
            return 0;
        }
        return repository.addAppUser(appUser);
    }

    @Override
    public int deleteAppUser(final long userId, final long appId) {
        return repository.deleteAppUser(userId, appId);
    }

    @Override
    public int deleteAppUserById(final long appUserId) {
        return repository.deleteAppUserById(appUserId);
    }

    @Override
    public ApplicationUser findAppUserById(long appUserId) {
        return repository.findAppUserById(appUserId);
    }

    @Override
    public ApplicationUser findAppUserByAppIdAndUserId(long appId, long userId) {
        return repository.findAppUserByAppIdAndUserId(appId, userId);
    }

    @Override
    public boolean belong(final long userId, final long appId) {
        return repository.belong(userId, appId);
    }

    @Override
    public List<User> findByWhereSql(String sql, Object obj) {
        return repository.findByWhereSql(ObjectUtil.replaceSql(obj, sql));
    }

    @Override
    public boolean validateWhereSql(String sql, Object obj) {
        List<User> users = findByWhereSql(sql, obj);
        if(NullUtil.isEmpty(users)) {
            return false;
        }
        return true;
    }

    @Override
    public List<User> findByRole(int role) {
        return repository.findByRole(role);
    }

}
