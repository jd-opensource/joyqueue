package io.openmessaging.jmq.extension;

import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.extension.QueueMetaData;

/**
 * TopicMetadataConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/1
 */
public class TopicMetadataConverter {

    public static QueueMetaData convert(TopicMetadata topicMetadata) {
        return new QueueMetaDataAdapter(topicMetadata);
    }
}