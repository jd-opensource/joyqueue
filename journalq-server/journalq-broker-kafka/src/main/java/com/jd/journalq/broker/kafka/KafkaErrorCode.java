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
package com.jd.journalq.broker.kafka;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.exception.LeaderNotAvailableException;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * KafkaErrorCode
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public final class KafkaErrorCode {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaErrorCode.class);

    private static final Map<Class<?>, Short> KAFKA_EXCEPTION_TO_CODE_MAPPER = Maps.newHashMap();
    private static final Map<Integer, Short> JMQCODE_TO_CODE_MAPPER = Maps.newHashMap();

    public static final short UNKNOWN = -1;
    public static final short NONE = 0;
    public static final short OFFSET_OUT_OF_RANGE = 1;
    public static final short INVALID_MESSAGE = 2;
    public static final short UNKNOWN_TOPIC_OR_PARTITION = 3;
    public static final short INVALID_FETCH_SIZE = 4;
    public static final short LEADER_NOT_AVAILABLE = 5;
    public static final short NOT_LEADER_FOR_PARTITION = 6;
    public static final short REQUEST_TIMEOUT = 7;
    public static final short BROKER_NOT_AVAILABLE = 8;
    public static final short REPLICA_NOT_AVAILABLE = 9;
    public static final short MESSAGE_SIZE_TOO_LARGE = 10;
    public static final short STALE_CONTROLLER_EPOCH = 11;
    public static final short OFFSET_METADATA_TOO_LARGE = 12;
    public static final short STALELEADER_EPOCH = 13;
    public static final short OFFSETS_LOAD_IN_PROGRESS = 14;
    public static final short GROUP_COORDINATOR_NOT_AVAILABLE = 15;
    public static final short NOT_COORDINATOR_FOR_CONSUMER = 16;
    public static final short INVALID_TOPIC = 17;
    public static final short MESSAGE_SET_SIZE_TOO_LARGE = 18;
    public static final short NOTENOUGH_REPLICAS = 19;
    public static final short NOTENOUGH_REPLICAS_AFTER_APPEND = 20;
    public static final short ILLEGAL_GENERATION = 22;
    public static final short INCONSISTENT_GROUP_PROTOCOL = 23;
    public static final short INVALID_GROUP_ID = 24;
    public static final short UNKNOWN_MEMBER_ID = 25;
    public static final short INVALID_SESSION_TIMEOUT = 26;
    public static final short REBALANCE_IN_PROGRESS = 27;
    public static final short TOPIC_AUTHORIZATION_FAILED = 29;
    public static final short UNSUPPORTED_VERSION = 35;

    static {
        // Kafka错误映射
        KAFKA_EXCEPTION_TO_CODE_MAPPER.put(LeaderNotAvailableException.class, NOT_LEADER_FOR_PARTITION);

        // JMQ错误映射
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.SUCCESS.getCode(), NONE);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_NO_PERMISSION.getCode(), UNKNOWN_TOPIC_OR_PARTITION);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_AUTHENTICATION_ERROR.getCode(), UNKNOWN_TOPIC_OR_PARTITION);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_SERVICE_NOT_AVAILABLE.getCode(), NOT_LEADER_FOR_PARTITION);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_CHECKSUM_ERROR.getCode(), INVALID_MESSAGE);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_CONNECTION_ERROR.getCode(), BROKER_NOT_AVAILABLE);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_CONNECTION_TIMEOUT.getCode(), BROKER_NOT_AVAILABLE);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_REQUEST_TIMEOUT.getCode(), REQUEST_TIMEOUT);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_REQUEST_ERROR.getCode(), REQUEST_TIMEOUT);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_REQUEST_EXCESSIVE.getCode(), REQUEST_TIMEOUT);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_THREAD_INTERRUPTED.getCode(), REQUEST_TIMEOUT);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CN_THREAD_EXECUTOR_BUSY.getCode(), REQUEST_TIMEOUT);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CT_NO_CLUSTER.getCode(), NOT_LEADER_FOR_PARTITION);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CT_MESSAGE_BODY_NULL.getCode(), INVALID_MESSAGE);
        JMQCODE_TO_CODE_MAPPER.put(JournalqCode.CY_REPLICATE_TIMEOUT.getCode(), REPLICA_NOT_AVAILABLE);
    }

    public static short exceptionFor(Throwable exception) {
        if (exception instanceof JournalqException) {
            return journalqCodeFor(((JournalqException) exception).getCode());
        } else {
            return kafkaExceptionFor(exception);
        }
    }

    public static short kafkaExceptionFor(Throwable exception) {
        Short code = KAFKA_EXCEPTION_TO_CODE_MAPPER.get(exception);
        if (code == null) {
            logger.warn("unsupported exception mapper, exception: {}", exception.getClass());
            code = UNKNOWN;
        }
        return code;
    }

    public static short journalqCodeFor(int journalqCode) {
        Short code = JMQCODE_TO_CODE_MAPPER.get(journalqCode);
        if (code == null) {
            logger.warn("unsupported journalqCode mapper, journalqCode: {}", journalqCode);
            code = UNKNOWN;
        }
        return code;
    }
}

