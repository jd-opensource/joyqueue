package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.command.Command;

/**
 * 布尔应答.
 *
 * @author lindeqiang
 * @since 2016/8/11 10:32
 */
public class BooleanAck extends Joyqueue0Payload {

    @Override
    public int type() {
        return Joyqueue0CommandType.BOOLEAN_ACK.getCode();
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
        Joyqueue0Header header = new Joyqueue0Header();
        header.setType(Joyqueue0CommandType.BOOLEAN_ACK.getCode());
        header.setStatus((short) code);
        header.setError(code == JoyQueueCode.SUCCESS.getCode() ? null : message);
        return new Command(header, null);
    }
}
