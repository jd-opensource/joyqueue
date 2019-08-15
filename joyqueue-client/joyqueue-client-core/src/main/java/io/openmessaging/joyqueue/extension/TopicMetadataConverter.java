package io.openmessaging.joyqueue.extension;

import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.extension.QueueMetaData;

/**
 * TopicMetadataConverter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class TopicMetadataConverter {

    public static QueueMetaData convert(TopicMetadata topicMetadata) {
        return new QueueMetaDataAdapter(topicMetadata);
    }
}