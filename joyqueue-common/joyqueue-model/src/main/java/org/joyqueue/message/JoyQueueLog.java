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
package org.joyqueue.message;


/**
 * 日志接口
 */
public interface JoyQueueLog {
    //日志类型
    byte TYPE_MESSAGE = (byte) 6;
    byte TYPE_TX_PREPARE = (byte) 3;
    byte TYPE_TX_COMMIT = (byte) 4;
    byte TYPE_TX_ROLLBACK = (byte) 5;

    int getStoreTime();

    byte getType();

    void setStoreTime(int storeTime);

    int getSize();

    String getTxId();

    long getStartTime();

}
