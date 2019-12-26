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
package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.model.ApiVersion;

import java.util.List;

/**
 * ApiVersionsResponse
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class ApiVersionsResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private List<ApiVersion> apis;

    public ApiVersionsResponse(short errorCode, List<ApiVersion> apis) {
        this.errorCode = errorCode;
        this.apis = apis;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public void setApis(List<ApiVersion> apis) {
        this.apis = apis;
    }

    public List<ApiVersion> getApis() {
        return apis;
    }

    @Override
    public int type() {
        return KafkaCommandType.API_VERSIONS.getCode();
    }
}