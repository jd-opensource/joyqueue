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
package com.jd.joyqueue.broker.kafka;

import com.google.common.collect.Maps;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.exception.JournalqException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * KafkaErrorCode
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public enum KafkaErrorCode {

    UNKNOWN_SERVER_ERROR(-1),
    NONE(0, JournalqCode.SUCCESS),
    OFFSET_OUT_OF_RANGE(1),
    CORRUPT_MESSAGE(2,
            JournalqCode.CN_CHECKSUM_ERROR, JournalqCode.CT_MESSAGE_BODY_NULL),
    UNKNOWN_TOPIC_OR_PARTITION(3,
            JournalqCode.CN_NO_PERMISSION, JournalqCode.CN_AUTHENTICATION_ERROR),
    INVALID_FETCH_SIZE(4),
    LEADER_NOT_AVAILABLE(5),
    NOT_LEADER_FOR_PARTITION(6, JournalqCode.CT_NO_CLUSTER),
    REQUEST_TIMED_OUT(7,
            JournalqCode.CN_REQUEST_TIMEOUT, JournalqCode.CN_REQUEST_ERROR, JournalqCode.CN_REQUEST_EXCESSIVE, JournalqCode.CN_THREAD_INTERRUPTED, JournalqCode.CN_THREAD_EXECUTOR_BUSY),
    BROKER_NOT_AVAILABLE(8,
            JournalqCode.CN_SERVICE_NOT_AVAILABLE, JournalqCode.CN_CONNECTION_ERROR, JournalqCode.CN_CONNECTION_TIMEOUT),
    REPLICA_NOT_AVAILABLE(9,
            JournalqCode.CY_REPLICATE_TIMEOUT, JournalqCode.CY_REPLICATE_ERROR, JournalqCode.CY_REPLICATE_ENQUEUE_TIMEOUT),
    MESSAGE_TOO_LARGE(10),
    STALE_CONTROLLER_EPOCH(11),
    OFFSET_METADATA_TOO_LARGE(12),
    NETWORK_EXCEPTION(13),
    COORDINATOR_LOAD_IN_PROGRESS(14),
    COORDINATOR_NOT_AVAILABLE(15),
    NOT_COORDINATOR(16),
    INVALID_TOPIC_EXCEPTION(17),
    RECORD_LIST_TOO_LARGE(18),
    NOT_ENOUGH_REPLICAS(19),
    NOT_ENOUGH_REPLICAS_AFTER_APPEND(20),
    INVALID_REQUIRED_ACKS(21),
    ILLEGAL_GENERATION(22),
    INCONSISTENT_GROUP_PROTOCOL(23),
    INVALID_GROUP_ID(24),
    UNKNOWN_MEMBER_ID(25),
    INVALID_SESSION_TIMEOUT(26),
    REBALANCE_IN_PROGRESS(27),
    INVALID_COMMIT_OFFSET_SIZE(28),
    TOPIC_AUTHORIZATION_FAILED(29),
    GROUP_AUTHORIZATION_FAILED(30),
    CLUSTER_AUTHORIZATION_FAILED(31),
    INVALID_TIMESTAMP(32),
    UNSUPPORTED_SASL_MECHANISM(33),
    ILLEGAL_SASL_STATE(34),
    UNSUPPORTED_VERSION(35),
    TOPIC_ALREADY_EXISTS(36),
    INVALID_PARTITIONS(37),
    INVALID_REPLICATION_FACTOR(38),
    INVALID_REPLICA_ASSIGNMENT(39),
    INVALID_CONFIG(40),
    NOT_CONTROLLER(41),
    INVALID_REQUEST(42),
    UNSUPPORTED_FOR_MESSAGE_FORMAT(43),
    POLICY_VIOLATION(44),
    OUT_OF_ORDER_SEQUENCE_NUMBER(45),
    DUPLICATE_SEQUENCE_NUMBER(46),
    INVALID_PRODUCER_EPOCH(47),
    INVALID_TXN_STATE(48),
    INVALID_PRODUCER_ID_MAPPING(49),
    INVALID_TRANSACTION_TIMEOUT(50),
    CONCURRENT_TRANSACTIONS(51),
    TRANSACTION_COORDINATOR_FENCED(52),
    TRANSACTIONAL_ID_AUTHORIZATION_FAILED(53),
    SECURITY_DISABLED(54),
    OPERATION_NOT_ATTEMPTED(55),
    KAFKA_STORAGE_ERROR(56),
    LOG_DIR_NOT_FOUND(57),
    SASL_AUTHENTICATION_FAILED(58),
    UNKNOWN_PRODUCER_ID(59),
    REASSIGNMENT_IN_PROGRESS(60),
    DELEGATION_TOKEN_AUTH_DISABLED(61),
    DELEGATION_TOKEN_NOT_FOUND(62),
    DELEGATION_TOKEN_OWNER_MISMATCH(63),
    DELEGATION_TOKEN_REQUEST_NOT_ALLOWED(64),
    DELEGATION_TOKEN_AUTHORIZATION_FAILED(65),
    DELEGATION_TOKEN_EXPIRED(66),
    INVALID_PRINCIPAL_TYPE(67),
    NON_EMPTY_GROUP(68),
    GROUP_ID_NOT_FOUND(69),
    FETCH_SESSION_ID_NOT_FOUND(70),
    INVALID_FETCH_SESSION_EPOCH(71),
    LISTENER_NOT_FOUND(72),
    TOPIC_DELETION_DISABLED(73),
    FENCED_LEADER_EPOCH(74),
    UNKNOWN_LEADER_EPOCH(75),
    UNSUPPORTED_COMPRESSION_TYPE(76),
    ;

    protected static final Logger logger = LoggerFactory.getLogger(KafkaErrorCode.class);

    private static final Map<Integer, Short> JOURNALQ_CODE_TO_CODE_MAPPER = Maps.newHashMap();
    private static final Map<Class<?>, Short> EXCEPTION_TO_CODE_MAPPER = Maps.newHashMap();

    static {
        for (KafkaErrorCode errorCode : KafkaErrorCode.values()) {
            if (errorCode.getJournalqCodes() == null) {
                continue;
            }
            for (JournalqCode JournalqCode : errorCode.getJournalqCodes()) {
                JOURNALQ_CODE_TO_CODE_MAPPER.put(JournalqCode.getCode(), errorCode.getCode());
            }
        }

        for (KafkaErrorCode errorCode : KafkaErrorCode.values()) {
            if (errorCode.getException() == null) {
                continue;
            }
            for (Throwable throwable : errorCode.getException()) {
                EXCEPTION_TO_CODE_MAPPER.put(throwable.getClass(), errorCode.getCode());
            }
        }
    }

    private int code;
    private JournalqCode[] JournalqCodes;
    private Throwable[] exception;

    KafkaErrorCode(int code) {
        this.code = code;
    }

    KafkaErrorCode(int code, Throwable... exception) {
        this.code = code;
        this.exception = exception;
    }

    KafkaErrorCode(int code, JournalqCode... JournalqCodes) {
        this.code = code;
        this.JournalqCodes = JournalqCodes;
    }

    KafkaErrorCode(int code, Throwable[] exception, JournalqCode[] JournalqCodes) {
        this.code = code;
        this.JournalqCodes = JournalqCodes;
        this.exception = exception;
    }

    public short getCode() {
        return (short) code;
    }

    public JournalqCode[] getJournalqCodes() {
        return JournalqCodes;
    }

    public Throwable[] getException() {
        return exception;
    }

    public static short exceptionFor(Throwable exception) {
        if (exception instanceof JournalqException) {
            return journalqCodeFor(((JournalqException) exception).getCode());
        } else {
            return kafkaExceptionFor(exception);
        }
    }

    public static short kafkaExceptionFor(Throwable exception) {
        Short code = EXCEPTION_TO_CODE_MAPPER.get(exception.getClass());
        if (code == null) {
            logger.warn("unsupported exception mapper, exception: {}", exception.getClass());
            return UNKNOWN_SERVER_ERROR.getCode();
        }
        return code;
    }

    public static short journalqCodeFor(int journalqCode) {
        Short code = JOURNALQ_CODE_TO_CODE_MAPPER.get(journalqCode);
        if (code == null) {
            logger.warn("unsupported code mapper, code: {}", JournalqCode.valueOf(journalqCode));
            code = UNKNOWN_SERVER_ERROR.getCode();
        }
        return code;
    }
}

