package com.jd.journalq.common.network.command;

import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Direction;
import com.jd.journalq.common.network.transport.command.JMQPayload;
import org.apache.commons.lang3.StringUtils;

/**
 * 布尔应答.
 *
 * @author lindeqiang
 * @since 2016/8/11 10:32
 */
public class BooleanAck extends JMQPayload {

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
        return build(JMQCode.SUCCESS);
    }

    /**
     * 构造布尔应答
     *
     * @param code
     * @param args
     * @return
     */
    public static Command build(final JMQCode code, Object... args) {
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
        JMQHeader header = new JMQHeader(Direction.RESPONSE, CommandType.BOOLEAN_ACK);
        header.setStatus((short) code);
        header.setError(code == JMQCode.SUCCESS.getCode() ? null : (StringUtils.isBlank(message) ? JMQCode.valueOf(code).getMessage() : message));
        return new Command(header, null);
    }
}
