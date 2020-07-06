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

import com.google.common.collect.Maps;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * App名称, 包含了订阅分组，供消费者使用
 */
public class AppName {
    public static final String DEFAULT_SUBSCRIBE_GROUP = "";
    public static final String APP_SEPARATOR = ".";
    public static final String APP_SEPARATOR_SPLIT = ".";

    private static final int APP_NAME_CACHE_SIZE = 10240;
    private static final ConcurrentMap<String, AppName> APP_NAME_CACHE = Maps.newConcurrentMap();

    private String code;
    private String subscribeGroup;
    private String fullName;

    public AppName() {
    }

    public AppName(String code) {
        this(code, DEFAULT_SUBSCRIBE_GROUP);
    }

    public AppName(String code, String subscribeGroup) {
        Preconditions.checkArgument(code != null && !code.isEmpty() && !code.contains(APP_SEPARATOR), "invalid app.");
        this.code = code;
        this.subscribeGroup = StringUtils.isBlank(subscribeGroup)?DEFAULT_SUBSCRIBE_GROUP : subscribeGroup;
    }

    public String getFullName() {
        if (fullName == null) {
            if (subscribeGroup == null || DEFAULT_SUBSCRIBE_GROUP.equals(subscribeGroup)) {
                fullName = code;
            } else {
                fullName = code + APP_SEPARATOR + subscribeGroup;
            }
        }
        return fullName;
    }

    public static AppName parse(String code, String subscribeGroup) {
        if (subscribeGroup == null || DEFAULT_SUBSCRIBE_GROUP.equals(subscribeGroup)) {
            subscribeGroup = DEFAULT_SUBSCRIBE_GROUP;
        }
        return new AppName(code, subscribeGroup);
    }

    public static AppName parse(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return null;
        }

        AppName appName = APP_NAME_CACHE.get(fullName);
        if (appName == null) {
            if (APP_NAME_CACHE.size() > APP_NAME_CACHE_SIZE) {
                APP_NAME_CACHE.clear();
            }
            String[] splits = StringUtils.splitByWholeSeparator(fullName, APP_SEPARATOR_SPLIT);
            if (splits.length == 1) {
                appName = new AppName(splits[0]);
            } else {
                appName = new AppName(splits[0], splits[1]);
            }
            APP_NAME_CACHE.put(fullName, appName);
        }
        return appName;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String app) {
        this.code = app;
    }

    public String getSubscribeGroup() {
        return subscribeGroup;
    }

    public void setSubscribeGroup(String subscribeGroup) {
        this.subscribeGroup = subscribeGroup;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppName that = (AppName) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(subscribeGroup, that.subscribeGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, subscribeGroup);
    }

    @Override
    public String toString() {
        return "AppName{" +
                "code='" + code + '\'' +
                ", subscribeGroup='" + subscribeGroup + '\'' +
                '}';
    }
}
