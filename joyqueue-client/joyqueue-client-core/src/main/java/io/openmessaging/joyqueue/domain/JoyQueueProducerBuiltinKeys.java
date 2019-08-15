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
 * JoyQueueProducerBuiltinKeys
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public interface JoyQueueProducerBuiltinKeys extends OMSBuiltinKeys {

    String TIMEOUT = "PRODUCER_TIMEOUT";

    String PRODUCE_TIMEOUT = "PRODUCER_PRODUCE_TIMEOUT";

    String TRANSACTION_TIMEOUT = "PRODUCER_TRANSACTION_TIMEOUT";

    String FAILOVER = "PRODUCER_FAILOVER";

    String RETRIES = "PRODUCER_RETRIES";

    String QOSLEVEL = "PRODUCER_QOSLEVEL";

    String COMPRESS = "PRODUCER_COMPRESS";

    String COMPRESS_TYPE = "PRODUCER_COMPRESS_TYPE";

    String COMPRESS_THRESHOLD = "PRODUCER_COMPRESS_THRESHOLD";

    String BATCH = "PRODUCER_BATCH";

    String SELECTOR_TYPE = "PRODUCER_SELECTOR_TYPE";

    String BUSINESSID_LENGTH_LIMIT = "PRODUCER_BUSINESSID_LENGTH_LIMIT";

    String BODY_LENGTH_LIMIT = "PRODUCER_BODY_LENGTH_LIMIT";

    String BATCH_BODY_LENGTH_LIMIT = "PRODUCER_BATCH_BODY_LENGTH_LIMIT";

}