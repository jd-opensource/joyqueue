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
package io.chubao.joyqueue.network.command;

import com.google.common.collect.Table;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchIndexResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
public class FetchIndexResponse extends JoyQueuePayload {

    private Table<String, Short, FetchIndexAckData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_INDEX_RESPONSE.getCode();
    }

    public void setData(Table<String, Short, FetchIndexAckData> data) {
        this.data = data;
    }

    public Table<String, Short, FetchIndexAckData> getData() {
        return data;
    }
}