package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.codec.NullPayloadCodec;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

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
