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
package io.openmessaging.joyqueue.domain;

import io.openmessaging.OMSBuiltinKeys;

/**
 * JoyQueueTxFeedbackBuiltinKeys
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public interface JoyQueueTxFeedbackBuiltinKeys extends OMSBuiltinKeys {

    String TIMEOUT = "TX_FEEDBACK_TIMEOUT";

    String LONGPOLL_TIMEOUT = "TX_FEEDBACK_LONGPOLL_TIMEOUT";

    String FETCH_INTERVAL = "TX_FEEDBACK_FETCH_INTERVAL";

    String FETCH_SIZE = "TX_FEEDBACK_FETCH_SIZE";
}