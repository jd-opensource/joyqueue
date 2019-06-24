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
package com.jd.journalq.broker.index.command;

import com.jd.journalq.network.transport.command.JournalqPayload;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.broker.index.model.IndexAndMetadata;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexStoreRequest extends JournalqPayload {
    private String app;
    private Map<String, Map<Integer, IndexAndMetadata>> indexMetadata;

    public ConsumeIndexStoreRequest(String app, Map<String, Map<Integer, IndexAndMetadata>> indexMetadata) {
        this.app = app;
        this.indexMetadata = indexMetadata;
    }

    public String getApp() {
        return app;
    }

    public Map<String, Map<Integer, IndexAndMetadata>> getIndexMetadata() {
        return indexMetadata;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }

    @Override
    public String toString() {
        return "ConsumeIndexStoreRequest{" +
                "app='" + app + '\'' +
                ", indexMetadata=" + indexMetadata +
                '}';
    }
}
