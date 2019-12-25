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
package org.joyqueue.handler.error;


import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * 异常码
 * Created by yangyang115 on 18-7-26.
 */
public enum ErrorCode {

    RuntimeError(900, HTTP_INTERNAL_ERROR, "运行时异常!"),

    DuplicateError(901, HTTP_INTERNAL_ERROR, "重复key异常!"),

    NoTipError(100, HTTP_INTERNAL_ERROR, "不提示异常"),

    ValidationError(300, HTTP_INTERNAL_ERROR, "code1,code2|验证不通过"),

    BadRequest(400, HTTP_BAD_REQUEST, "请求参数无效!"),

    ServiceError(500, HTTP_INTERNAL_ERROR, "内部服务请求错误!"),

    NoDataExists(501, HTTP_NOT_FOUND, "数据不存在!"),
    NoDataAdded(502, HTTP_NOT_FOUND, "新增不成功，没有数据被新增!"),
    NoDataUpdated(503, HTTP_NOT_FOUND, "要更新的数据不存在!"),
    NoDataDeleted(504, HTTP_NOT_FOUND, "要删除的数据不存在!"),

    ConfigurationNotExists(506, HTTP_NOT_FOUND, "配置不存在!"),
    ApplicationNotExists(505, HTTP_NOT_FOUND, "应用不存在!"),
    AppTokenNotExists(507, HTTP_NOT_FOUND, "应用令牌不存在!"),
    UserNotExists(508, HTTP_NOT_FOUND, "用户不存在!"),
    AppUserNotExists(509, HTTP_NOT_FOUND, "应用用户不存在!"),

    NoPrivilege(511, HTTP_FORBIDDEN, "无权操作!"),

    InvalidToken(521, HTTP_UNAUTHORIZED, "令牌无效!"),
    ExcessiveToken(522, HTTP_UNAUTHORIZED, "应用令牌过多!"),
    InvalidConfiguration(523, HTTP_UNAUTHORIZED, "配置有问题！"),

    LoginError(531, HTTP_FORBIDDEN, "用户没有登录，请登录!"),

    SQLError(600, HTTP_INTERNAL_ERROR, "SQL执行错误");


    //业务异常码
    int code;
    //HTPP响应码
    int status;
    //消息
    String message;

    ErrorCode(int code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

}
