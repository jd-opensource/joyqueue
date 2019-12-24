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
package org.joyqueue.nsr.network.codec;

import org.joyqueue.network.codec.NullPayloadCodec;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/2/14
 */
public class NsrNullPayLoadCodec extends NullPayloadCodec implements NsrPayloadCodec {
    @Override
    public int[] types() {
        return new int[]{CommandType.BOOLEAN_ACK,
                NsrCommandType.GET_ALL_BROKERS,
                NsrCommandType.GET_ALL_TOPICS,
                NsrCommandType.GET_ALL_CONFIG,
                NsrCommandType.PUSH_NAMESERVER_EVENT_ACK,
                NsrCommandType.LEADER_REPORT_ACK};
    }
}
