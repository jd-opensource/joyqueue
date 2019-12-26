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

import java.util.Calendar;
import java.util.Date;

/**
 * 持续时间
 */
public interface DurationTime {

    /**
     * 生效时间
     *
     * @return
     */
    Date getEffectiveTime();

    /**
     * 设置生效时间
     *
     * @param time
     */
    void setEffectiveTime(Date time);

    /**
     * 过期时间
     *
     * @return
     */
    Date getExpirationTime();

    /**
     * 设置过期时间
     *
     * @param time
     */
    void setExpirationTime(Date time);

    /**
     * 时间是否有效
     *
     * @param time 时间
     * @return
     */
    default boolean isEffective(final Date time) {
        if (time == null) {
            return false;
        }
        Date effectiveTime = getEffectiveTime();
        Date expirationTime = getExpirationTime();
        if (effectiveTime != null) {
            if (effectiveTime.after(time)) {
                return false;
            }
        }
        if (expirationTime != null) {
            if (expirationTime.before(time)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化时间
     */
    default void initializeTime() {
        initializeTime(10);
    }

    /**
     * 初始化时间
     *
     * @param expireYears 在几年以后过期
     */
    default void initializeTime(int expireYears) {
        Date now = new Date();
        if (getEffectiveTime() == null) {
            setEffectiveTime(now);
        }
        if (getExpirationTime() == null) {
            Calendar cd = Calendar.getInstance();
            cd.setTime(now);
            cd.add(Calendar.YEAR, expireYears);
            setExpirationTime(cd.getTime());
        }
    }
}
