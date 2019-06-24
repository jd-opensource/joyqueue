/**
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
package com.jd.joyqueue.broker.protocol.converter;

import com.jd.joyqueue.exception.JournalqCode;
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

    public static JournalqCode convertProduceCode(JournalqCode code) {
        switch (code) {
            case FW_TOPIC_NOT_EXIST: {
                return JournalqCode.FW_TOPIC_NOT_EXIST;
            }
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER:
            case FW_TOPIC_NO_PARTITIONGROUP: {
                return JournalqCode.FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER;
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE: {
                return JournalqCode.FW_PUT_MESSAGE_TOPIC_NOT_WRITE;
            }
            case FW_PRODUCER_NOT_EXISTS: {
                return JournalqCode.CN_NO_PERMISSION;
            }
            default : {
                logger.warn("unknown produce code {}", code);
                return JournalqCode.CN_NO_PERMISSION;
            }
        }
    }

    public static JournalqCode convertFetchCode(JournalqCode code) {
        switch (code) {
            case FW_TOPIC_NOT_EXIST: {
                return JournalqCode.FW_TOPIC_NOT_EXIST;
            }
            case FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER:
            case FW_TOPIC_NO_PARTITIONGROUP: {
                return JournalqCode.FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER;
            }
            case FW_GET_MESSAGE_APP_CLIENT_IP_NOT_READ: {
                return JournalqCode.FW_GET_MESSAGE_TOPIC_NOT_READ;
            }
            case FW_FETCH_TOPIC_MESSAGE_PAUSED: {
                return JournalqCode.FW_FETCH_TOPIC_MESSAGE_PAUSED;
            }
            case FW_CONSUMER_NOT_EXISTS: {
                return JournalqCode.FW_CONSUMER_NOT_EXISTS;
            }
            default : {
                logger.warn("unknown fetch code {}", code);
                return JournalqCode.CN_NO_PERMISSION;
            }
        }
    }

    public static JournalqCode convertCommonCode(JournalqCode code) {
        switch (code) {
            case FW_TOPIC_NO_PARTITIONGROUP:
            case FW_TOPIC_NOT_EXIST: {
                return JournalqCode.CN_NO_PERMISSION;
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE:
            case FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER:
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER: {
                return JournalqCode.CN_NO_PERMISSION;
            }
            default : {
                logger.warn("unknown common code {}", code);
                return JournalqCode.CN_NO_PERMISSION;
            }
        }
    }
}