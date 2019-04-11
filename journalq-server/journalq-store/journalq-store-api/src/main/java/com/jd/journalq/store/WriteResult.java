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
package com.jd.journalq.store;

import com.jd.journalq.exception.JMQCode;

/**
 * 写消息结果
 */
public class WriteResult {
    private long [] metrics;

    public WriteResult() {}
    public WriteResult(JMQCode code, long [] indices) {
        this. code = code;
        this.indices = indices;
    }
    public WriteResult(JMQCode code, long [] indices, long [] metrics) {
        this. code = code;
        this.indices = indices;
        this.metrics = metrics;
    }
    /**
     * 状态码
     */
    private JMQCode code;

    /**
     * 写入消息的在partition中的序号
     */
    private long [] indices;

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public long[] getIndices() {
        return indices;
    }

    public void setIndices(long[] indices) {
        this.indices = indices;
    }

    public long[] getMetrics() {
        return metrics;
    }

    public void setMetrics(long[] metrics) {
        this.metrics = metrics;
    }
}
