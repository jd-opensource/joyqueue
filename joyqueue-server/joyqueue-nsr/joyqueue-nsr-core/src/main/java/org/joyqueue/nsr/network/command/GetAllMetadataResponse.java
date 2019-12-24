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
package org.joyqueue.nsr.network.command;

import org.joyqueue.domain.AllMetadata;
import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * GetAllMetadataResponse
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class GetAllMetadataResponse extends JoyQueuePayload {

    private AllMetadata metadata;
    private byte[] response;

    public void setMetadata(AllMetadata metadata) {
        this.metadata = metadata;
    }

    public AllMetadata getMetadata() {
        return metadata;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public byte[] getResponse() {
        return response;
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_GET_ALL_METADATA_RESPONSE;
    }
}