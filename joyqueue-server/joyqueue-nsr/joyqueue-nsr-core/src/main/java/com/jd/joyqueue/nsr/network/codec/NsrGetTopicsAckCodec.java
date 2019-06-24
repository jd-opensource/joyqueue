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
package com.jd.joyqueue.nsr.network.codec;

import com.jd.joyqueue.network.codec.GetTopicsAckCodec;
import com.jd.joyqueue.network.command.GetTopicsAck;
import com.jd.joyqueue.network.transport.command.Types;
import com.jd.joyqueue.nsr.network.NsrPayloadCodec;
import com.jd.joyqueue.nsr.network.command.NsrCommandType;


/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrGetTopicsAckCodec extends GetTopicsAckCodec implements NsrPayloadCodec<GetTopicsAck>, Types {

    @Override
    public int[] types() {
        return new int[]{NsrCommandType.GET_TOPICS_ACK,NsrCommandType.MQTT_GET_TOPICS_ACK};
    }
}
