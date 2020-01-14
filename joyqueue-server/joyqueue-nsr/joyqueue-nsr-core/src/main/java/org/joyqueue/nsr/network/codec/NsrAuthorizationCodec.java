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

import org.joyqueue.network.codec.AuthorizationCodec;
import org.joyqueue.network.command.Authorization;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.NsrCommandType;

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
