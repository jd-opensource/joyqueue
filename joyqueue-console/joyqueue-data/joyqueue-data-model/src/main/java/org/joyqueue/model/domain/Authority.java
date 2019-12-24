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
package org.joyqueue.model.domain;

import java.util.Date;
import java.util.List;

/**
 * Created by yangyang115 on 18-7-26.
 */
public abstract class Authority extends BaseModel implements DurationTime {

    protected Identity application;
    protected int authority;
    protected Date effectiveTime;
    protected Date expirationTime;

    public Identity getApplication() {
        return application;
    }

    public void setApplication(Identity application) {
        this.application = application;
    }

    public int getAuthority() {
        return authority;
    }

    public void setAuthority(int authority) {
        this.authority = authority;
    }

    public Date getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Date effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean hasWrite() {
        return authority == AuthorityType.RW.value;
    }

    /**
     * 验证权限
     *
     * @param authorities
     * @param writable
     * @return
     */
    public static <T extends Authority> boolean validate(final List<T> authorities, final boolean writable) {
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        Date now = new Date();
        AuthorityType type;
        for (Authority privilege : authorities) {
            //判断权限是否生效
            if (privilege.isEffective(now)) {
                type = AuthorityType.valueOf(privilege.getAuthority());
                if (!writable && type.isReadable() || writable && type.isWritable()) {
                    //写读权限
                    return true;
                }
            }
        }
        return false;
    }

    public enum AuthorityType {

        R(0, "read"),
        RW(1, "read and write") {
            @Override
            public boolean isWritable() {
                return true;
            }
        };

        private int value;
        private String description;

        AuthorityType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return value;
        }

        public String description() {
            return description;
        }

        public boolean isReadable() {
            return true;
        }

        public boolean isWritable() {
            return false;
        }

        /**
         * 读写权限
         *
         * @param type
         * @return
         */
        public static AuthorityType valueOf(int type) {
            switch (type) {
                case 1:
                    return RW;
                default:
                    return R;
            }
        }
    }
}
