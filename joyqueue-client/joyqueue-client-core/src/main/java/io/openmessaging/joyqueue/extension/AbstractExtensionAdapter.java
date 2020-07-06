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
package io.openmessaging.joyqueue.extension;

import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import com.google.common.base.Preconditions;
import io.openmessaging.extension.Extension;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * AbstractExtensionAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public abstract class AbstractExtensionAdapter implements Extension {

    @Override
    public QueueMetaData getQueueMetaData(String queueName) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");

            TopicMetadata topicMetadata = getTopicMetadata(queueName);
            if (topicMetadata == null) {
                return null;
            }
            return TopicMetadataConverter.convert(topicMetadata);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertRuntimeException(cause);
        }
    }

    protected abstract TopicMetadata getTopicMetadata(String queueName);
}