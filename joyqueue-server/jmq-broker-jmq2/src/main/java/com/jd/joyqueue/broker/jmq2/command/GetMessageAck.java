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
package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import org.joyqueue.broker.network.traffic.FetchResponseTrafficPayload;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.command.Command;

import java.util.List;

/**
 * 请求消息应答
 */
public class GetMessageAck extends JMQ2Payload implements FetchResponseTrafficPayload {
    // 存储的消息
    protected List<BrokerMessage> messages;
    protected Traffic traffic;

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }

    /**
     * 构造应答
     *
     * @return 布尔应答
     */
    public static Command build() {
        return build(JoyQueueCode.SUCCESS);
    }

    /**
     * 构造应答
     *
     * @param code
     * @param args
     * @return
     */
    public static Command build(final JoyQueueCode code, Object... args) {
        return build(code.getCode(), code.getMessage(args));
    }

    /**
     * 构造应答
     *
     * @param code 代码
     * @return 布尔应答
     */
    public static Command build(int code, String error) {
        JMQ2Header header = new JMQ2Header();
        header.setStatus(code);
        header.setError(error);
        header.setType(JMQ2CommandType.GET_MESSAGE_ACK.getCode());
        GetMessageAck getMessageAck = new GetMessageAck();
        return new Command(header, getMessageAck);
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_MESSAGE_ACK.getCode();
    }
}