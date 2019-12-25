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
package org.joyqueue.network.command;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.apache.commons.lang3.StringUtils;

/**
 * 布尔应答.
 *
 * @author lindeqiang
 * @since 2016/8/11 10:32
 */
public class BooleanAck extends JoyQueuePayload {

    @Override
    public int type() {
        return CommandType.BOOLEAN_ACK;
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
     * @param code 状态码
     * @param args 参数
     * @return 命令
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
        JoyQueueHeader header = new JoyQueueHeader(Direction.RESPONSE, CommandType.BOOLEAN_ACK);
        header.setStatus((short) code);
        header.setError(code == JoyQueueCode.SUCCESS.getCode() ? null : (StringUtils.isBlank(message) ? JoyQueueCode.valueOf(code).getMessage() : message));
        return new Command(header, null);
    }
}
