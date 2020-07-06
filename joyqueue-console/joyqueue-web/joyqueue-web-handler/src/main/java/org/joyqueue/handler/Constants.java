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
package org.joyqueue.handler;

import com.jd.laf.web.vertx.Environment;

/**
 * 常量
 */
public interface Constants {

    /**
     * 对象ID
     */
    String ID = "id";
    /**
     * 对象ID List
     */
    String IDS = "ids";
    /**
     * CODE
     */
    String CODE = "code";
    /**
     * grafana dashbord UID
     */
    String UID = "uid";
    /**
     * 管理员
     */
    String ADMIN = "admin";
    /**
     * 关键词
     */
    String KEYWORD = "keyword";
    /**
     * 应用代码
     */
    String APP_CODE = "appCode";
    /**
     * 应用ID
     */
    String APP_ID = "appId";
    /**
     * 应用
     */
    String APPLICATION = "application";
    /**
     * 归档
     */
    String ARCHIVE = "archive";
    /**
     * 表单类型
     */
    String FORM_TYPE="formType";
    /**
     * 令牌
     */
    String TOKEN = "token";
    /**
     * 应用令牌ID
     */
    String APP_TOKEN_ID = "appTokenId";
    /**
     * 应用令牌
     */
    String APP_TOKEN = "appToken";
    /**
     * 应用用户ID
     */
    String APP_USER_ID = "appUserId";
    /**
     * 应用用户
     */
    String APP_USER = "appUser";
    /**
     * 角色
     */
    String ROLE = "role";
    /**
     * 用户会话的键
     */
    String USER_KEY = Environment.USER_KEY;

    /**
     * 配置ID
     */
    String CONFIG_ID = "configId";
    /**
     * 配置代码
     */
    String CONFIG_CODE = "config";
    /**
     * 配置
     */
    String CONFIG = "config";
    /**
     * 配置所属应用的成员
     */
    String CONFIG_OWNER_MEMBER = "configOwnerMember";
    /**
     * 用户ID
     */
    String USER_ID = "userId";

    String LONG_POLLING_DAEMON = "longPollingDaemon";

    String CACHE_CLEAR_DAEMON = "cacheClearDaemon";

    /**
     * 标识符最大长度
     */
    String IDENTIFIER_MAX_LENGTH = "identifierMaxLength";

    String UNIQUE_ERROR = "UniqueError";

    /**
     * 起始
     */
    String START = "start";

    /**
     * 页码
     */
    String PAGE = "page";

    /**
     * 条数
     */
    String SIZE = "size";

    /**
     * 名称
     */
    String NAME = "name";
    /**
     * 状态
     */
    String STATUS = "status";
    /**
     * 分组
     */
    String GROUP = "group";
    /**
     * key
     */
    String KEY = "key";

    /**
     * args
     */
    String args = "args";
    /**
     * args
     */
    String args_types = "args_types";
    /**
     * broker
     */
    String BROKER = "broker";
    /**
     * topic
     */
    String TOPIC = "topic";
    /**
     * metric
     */
    String METRIC = "metric";
}
