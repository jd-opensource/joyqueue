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

/**
 * ApiVersionsRequest
 *
 * @author luoruiheng
 * @since 1/5/18
 */
public class ApiVersionsRequest extends KafkaRequestOrResponse {

    private String clientSoftwareName;
    private String clientSoftwareVersion;

    public String getClientSoftwareName() {
        return clientSoftwareName;
    }

    public void setClientSoftwareName(String clientSoftwareName) {
        this.clientSoftwareName = clientSoftwareName;
    }

    public String getClientSoftwareVersion() {
        return clientSoftwareVersion;
    }

    public void setClientSoftwareVersion(String clientSoftwareVersion) {
        this.clientSoftwareVersion = clientSoftwareVersion;
    }

    @Override
    public int type() {
        return KafkaCommandType.API_VERSIONS.getCode();
    }
}