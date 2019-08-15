package io.chubao.joyqueue.broker.protocol.converter;

import io.chubao.joyqueue.exception.JoyQueueCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CheckResultConverter
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class CheckResultConverter {

    protected static final Logger logger = LoggerFactory.getLogger(CheckResultConverter.class);

    public static JoyQueueCode convertProduceCode(JoyQueueCode code) {
        switch (code) {
            case FW_TOPIC_NOT_EXIST: {
                return JoyQueueCode.FW_TOPIC_NOT_EXIST;
            }
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER:
            case FW_TOPIC_NO_PARTITIONGROUP: {
                return JoyQueueCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER;
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE: {
                return JoyQueueCode.FW_PUT_MESSAGE_TOPIC_NOT_WRITE;
            }
            case FW_PRODUCER_NOT_EXISTS: {
                return JoyQueueCode.FW_PRODUCER_NOT_EXISTS;
            }
            case FW_BROKER_NOT_WRITABLE: {
                return JoyQueueCode.FW_BROKER_NOT_WRITABLE;
            }
            default : {
                logger.warn("unknown produce code {}", code);
                return JoyQueueCode.CN_NO_PERMISSION;
            }
        }
    }

    public static JoyQueueCode convertFetchCode(JoyQueueCode code) {
        switch (code) {
            case FW_TOPIC_NOT_EXIST: {
                return JoyQueueCode.FW_TOPIC_NOT_EXIST;
            }
            case FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER:
            case FW_TOPIC_NO_PARTITIONGROUP: {
                return JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER;
            }
            case FW_GET_MESSAGE_APP_CLIENT_IP_NOT_READ: {
                return JoyQueueCode.FW_GET_MESSAGE_TOPIC_NOT_READ;
            }
            case FW_FETCH_TOPIC_MESSAGE_PAUSED: {
                return JoyQueueCode.FW_FETCH_TOPIC_MESSAGE_PAUSED;
            }
            case FW_CONSUMER_NOT_EXISTS: {
                return JoyQueueCode.FW_CONSUMER_NOT_EXISTS;
            }
            case FW_BROKER_NOT_READABLE: {
                return JoyQueueCode.FW_BROKER_NOT_READABLE;
            }
            default : {
                logger.warn("unknown fetch code {}", code);
                return JoyQueueCode.CN_NO_PERMISSION;
            }
        }
    }

    public static JoyQueueCode convertCommonCode(JoyQueueCode code) {
        switch (code) {
            case FW_TOPIC_NO_PARTITIONGROUP:
            case FW_TOPIC_NOT_EXIST: {
                return JoyQueueCode.CN_NO_PERMISSION;
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE:
            case FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER:
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER: {
                return JoyQueueCode.CN_NO_PERMISSION;
            }
            default : {
                logger.warn("unknown common code {}", code);
                return JoyQueueCode.CN_NO_PERMISSION;
            }
        }
    }
}