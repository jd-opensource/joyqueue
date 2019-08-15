package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Direction;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
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
        JoyQueueHeader header = new JoyQueueHeader(Direction.RESPONSE, CommandType.BOOLEAN_ACK);
        header.setStatus((short) code);
        header.setError(code == JoyQueueCode.SUCCESS.getCode() ? null : (StringUtils.isBlank(message) ? JoyQueueCode.valueOf(code).getMessage() : message));
        return new Command(header, null);
    }
}
