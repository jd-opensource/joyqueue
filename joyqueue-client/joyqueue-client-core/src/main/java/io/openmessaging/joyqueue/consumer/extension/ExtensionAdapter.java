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
package io.openmessaging.joyqueue.consumer.extension;

import org.joyqueue.client.internal.consumer.MessageConsumer;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.joyqueue.extension.AbstractExtensionAdapter;

/**
 * ExtensionAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class ExtensionAdapter extends AbstractExtensionAdapter {

    private MessageConsumer messageConsumer;

    public ExtensionAdapter(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    protected TopicMetadata getTopicMetadata(String queueName) {
        return messageConsumer.getTopicMetadata(queueName);
    }
}