package org.joyqueue.service.impl;

import org.joyqueue.service.MessageDeserializer;
import org.joyqueue.service.MessagePreviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预览消息服务实现类
 * @author LiYue
 * Date: 2020/2/8
 */
@Service("messagePreviewService")
public class MessagePreviewServiceImpl implements MessagePreviewService {
    private static final Logger logger = LoggerFactory.getLogger(MessagePreviewServiceImpl.class);
    private final Map<String, MessageDeserializer> messageDeserializerMap;
    private final List<MessageDeserializer> messageDeserializers;
    @Autowired
    public MessagePreviewServiceImpl(List<MessageDeserializer> messageDeserializers, MessageDeserializer defaultMessageDeserializer) {
        this.messageDeserializers = messageDeserializers;

        // move default serializer to the head of the list
        messageDeserializers.remove(defaultMessageDeserializer);
        messageDeserializers.add(0, defaultMessageDeserializer);

        messageDeserializerMap = messageDeserializers.stream().collect(Collectors.toMap(
                MessageDeserializer::getSerializeTypeName, d -> d
        ));
    }

    @PostConstruct
    public void printTypeNames() {
        logger.info("Supported message types: {}.", getMessageTypeNames());
    }
    @Override
    public List<String> getMessageTypeNames() {
        return messageDeserializers.stream().map(MessageDeserializer::getSerializeTypeName).collect(Collectors.toList());
    }

    @Override
    public String preview(String typeName, byte[] binaryMessage) {
        return getMessageDeserializer(typeName).deserialize(binaryMessage);
    }

    @Override
    public List<String> preview(String typeName, List<byte[]> binaryMessages) {
        return getMessageDeserializer(typeName).deserialize(binaryMessages);
    }

    private MessageDeserializer getMessageDeserializer(String typeName) {
        return messageDeserializerMap.getOrDefault(typeName, messageDeserializers.get(0));
    }
}
