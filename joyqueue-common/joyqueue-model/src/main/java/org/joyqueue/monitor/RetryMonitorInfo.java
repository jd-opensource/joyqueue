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
package org.joyqueue.monitor;

/**
 * 重试信息
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class RetryMonitorInfo extends BaseMonitorInfo {

    private long count;
    private long success;
    private long failure;
    private long current;

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFailure() {
        return failure;
    }

    public void setFailure(long failure) {
        this.failure = failure;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getCurrent() {
        return current;
    }
}