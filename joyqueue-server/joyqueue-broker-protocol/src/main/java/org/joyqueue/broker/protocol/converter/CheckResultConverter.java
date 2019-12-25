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
package org.joyqueue.broker.protocol.converter;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
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

    public static JoyQueueCode convertProduceCode(int version, JoyQueueCode code) {
        switch (code) {
            case FW_TOPIC_NOT_EXIST: {
                return JoyQueueCode.FW_TOPIC_NOT_EXIST;
            }
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER:
            case FW_TOPIC_NO_PARTITIONGROUP: {
                return JoyQueueCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER;
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE: {
                if (version == JoyQueueHeader.VERSION_V1) {
                    return JoyQueueCode.CN_NO_PERMISSION;
                }
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

    public static JoyQueueCode convertFetchCode(int version, JoyQueueCode code) {
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
                if (version == JoyQueueHeader.VERSION_V1) {
                    return JoyQueueCode.CN_NO_PERMISSION;
                }
                return JoyQueueCode.FW_BROKER_NOT_READABLE;
            }
            default : {
                logger.warn("unknown fetch code {}", code);
                return JoyQueueCode.CN_NO_PERMISSION;
            }
        }
    }

    public static JoyQueueCode convertCommonCode(int version, JoyQueueCode code) {
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