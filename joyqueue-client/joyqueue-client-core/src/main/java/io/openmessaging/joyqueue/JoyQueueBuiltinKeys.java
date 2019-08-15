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
package io.openmessaging.joyqueue;

import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueConsumerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueNameServerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueProducerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueTransportBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueTxFeedbackBuiltinKeys;

/**
 * JoyQueueBuiltinKeys
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public interface JoyQueueBuiltinKeys extends OMSBuiltinKeys, JoyQueueNameServerBuiltinKeys, JoyQueueTransportBuiltinKeys,
        JoyQueueProducerBuiltinKeys, JoyQueueConsumerBuiltinKeys, JoyQueueTxFeedbackBuiltinKeys {

}