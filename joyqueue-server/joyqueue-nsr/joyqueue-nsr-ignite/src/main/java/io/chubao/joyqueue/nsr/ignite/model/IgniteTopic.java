package io.chubao.joyqueue.nsr.ignite.model;

import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.domain.TopicName;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author lixiaobin6
 * 下午2:32 2018/8/13
 */
public class IgniteTopic extends Topic implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_PARTITIONS = "partitions";
    public static final String COLUMN_PRIORITY_PARTITIONS = "priority_partitions";
    public static final String COLUMN_TYPE = "type";
    /**
     * id:namespace+topic
     */

    public IgniteTopic(Topic topic) {
        this.type = topic.getType();
        this.name = topic.getName();
        this.partitions = topic.getPartitions();
    }


    @Override
    public String getId() {
        return name.getFullName();
    }

    public static String getId(String namespace, String topic) {
        return new StringBuilder(30).append(namespace).append(SPLICE).append(topic).toString();
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_NAMESPACE, name.getNamespace());
        writer.writeString(COLUMN_CODE, name.getCode());
        writer.writeShort(COLUMN_PARTITIONS, partitions);
        writer.writeByte(COLUMN_TYPE, type.code());
        if (null != priorityPartitions) {
            writer.writeString(COLUMN_PRIORITY_PARTITIONS, Arrays.toString(priorityPartitions.toArray()));
        }
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        //this.id = reader.readString(COLUMN_ID);
        String namespace = reader.readString(COLUMN_NAMESPACE);
        String topicCode = reader.readString(COLUMN_CODE);
        this.name = new TopicName(topicCode, namespace);

        this.partitions = reader.readShort(COLUMN_PARTITIONS);
        this.type = Type.valueOf(reader.readByte(COLUMN_TYPE));

        String priorityPartitionsStr = reader.readString(COLUMN_PRIORITY_PARTITIONS);
        this.priorityPartitions = new TreeSet<>();
        if (StringUtils.isNoneEmpty(priorityPartitionsStr) && priorityPartitionsStr.length() > 2) {
            this.priorityPartitions.addAll(
                    (Arrays.stream(priorityPartitionsStr.substring(1, priorityPartitionsStr.length() - 1).split(",")).
                            map(s -> Short.parseShort(s.trim())).collect(Collectors.toSet()))
            );
        }
    }
}
