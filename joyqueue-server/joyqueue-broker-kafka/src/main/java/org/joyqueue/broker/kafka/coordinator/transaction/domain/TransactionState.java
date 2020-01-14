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
package org.joyqueue.broker.kafka.coordinator.transaction.domain;

/**
 * TransactionState
 *
 * author: gaohaoxiang
 * date: 2019/4/11
 */
public enum TransactionState {

    EMPTY(1),
    ONGOING(2),
    PREPARE_COMMIT(3),
    PREPARE_ABORT(4),
    COMPLETE_COMMIT(5),
    COMPLETE_ABORT(6),
    DEAD(7)

    ;

    private int value;

    TransactionState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TransactionState valueOf(int value) {
        switch (value) {
            case 1:
                return EMPTY;
            case 2:
                return ONGOING;
            case 3:
                return PREPARE_COMMIT;
            case 4:
                return PREPARE_ABORT;
            case 5:
                return COMPLETE_COMMIT;
            case 6:
                return COMPLETE_ABORT;
            case 7:
                return DEAD;
        }
        return null;
    }
}