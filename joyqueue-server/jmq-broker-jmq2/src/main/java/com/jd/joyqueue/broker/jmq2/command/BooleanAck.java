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
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.command.Command;

/**
 * 布尔应答.
 *
 * @author lindeqiang
 * @since 2016/8/11 10:32
 */
public class BooleanAck extends JMQ2Payload {

    @Override
    public int type() {
        return JMQ2CommandType.BOOLEAN_ACK.getCode();
    }

    /**
     * 构造布尔应答
     *
     * @return 布尔应答
     */
    public static Command build() {
        return build(JoyQueueCode.SUCCESS);
    }

    /**
     * 构造布尔应答
     *
     * @param code
     * @param args
     * @return
     */
    public static Command build(final JoyQueueCode code, Object... args) {
        return build(code.getCode(), code.getMessage(args));
    }

    /**
     * 构造布尔应答
     *
     * @param code 代码
     * @return 布尔应答
     */
    public static Command build(final int code) {
        return build(code, null);
    }

    /**
     * 构造布尔应答
     *
     * @param code    代码
     * @param message 消息
     * @return 布尔应答
     */
    public static Command build(final int code, final String message) {
        JMQ2Header header = new JMQ2Header();
        header.setType(JMQ2CommandType.BOOLEAN_ACK.getCode());
        header.setStatus((short) code);
        header.setError(code == JoyQueueCode.SUCCESS.getCode() ? null : message);
        return new Command(header, null);
    }
}
