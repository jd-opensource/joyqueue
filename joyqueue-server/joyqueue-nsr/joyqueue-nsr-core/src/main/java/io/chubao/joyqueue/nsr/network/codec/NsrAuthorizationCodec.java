package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.codec.AuthorizationCodec;
import io.chubao.joyqueue.network.command.Authorization;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/3/20
 */
public class NsrAuthorizationCodec extends AuthorizationCodec implements NsrPayloadCodec<Authorization> {
    @Override
    public int type() {
        return NsrCommandType.AUTHORIZATION;
    }
}
