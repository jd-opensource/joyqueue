package com.jd.journalq.broker.jmq.converter;

import com.jd.journalq.exception.JMQCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CheckResultConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public class CheckResultConverter {

    protected static final Logger logger = LoggerFactory.getLogger(CheckResultConverter.class);

    public static JMQCode convertProduceCode(JMQCode code) {
        switch (code) {
            case FW_TOPIC_NOT_EXIST: {
                return JMQCode.FW_TOPIC_NOT_EXIST;
            }
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER:
            case FW_TOPIC_NO_PARTITIONGROUP: {
                return JMQCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER;
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE: {
                return JMQCode.FW_PUT_MESSAGE_TOPIC_NOT_WRITE;
            }
            case FW_PRODUCER_NOT_EXISTS: {
                return JMQCode.CN_NO_PERMISSION;
            }
            default : {
                logger.warn("unknown produce code {}", code);
                return JMQCode.CN_NO_PERMISSION;
            }
        }
    }

    public static JMQCode convertFetchCode(JMQCode code) {
        switch (code) {
            case FW_TOPIC_NOT_EXIST: {
                return JMQCode.FW_TOPIC_NOT_EXIST;
            }
            case FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER:
            case FW_TOPIC_NO_PARTITIONGROUP: {
                return JMQCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER;
            }
            case FW_GET_MESSAGE_APP_CLIENT_IP_NOT_READ: {
                return JMQCode.FW_GET_MESSAGE_TOPIC_NOT_READ;
            }
            case FW_FETCH_TOPIC_MESSAGE_PAUSED: {
                return JMQCode.FW_FETCH_TOPIC_MESSAGE_PAUSED;
            }
            case FW_CONSUMER_NOT_EXISTS: {
                return JMQCode.FW_CONSUMER_NOT_EXISTS;
            }
            default : {
                logger.warn("unknown fetch code {}", code);
                return JMQCode.CN_NO_PERMISSION;
            }
        }
    }

    public static JMQCode convertCommonCode(JMQCode code) {
        switch (code) {
            case FW_TOPIC_NO_PARTITIONGROUP:
            case FW_TOPIC_NOT_EXIST: {
                return JMQCode.CN_NO_PERMISSION;
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE:
            case FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER:
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER: {
                return JMQCode.CN_NO_PERMISSION;
            }
            default : {
                logger.warn("unknown common code {}", code);
                return JMQCode.CN_NO_PERMISSION;
            }
        }
    }
}