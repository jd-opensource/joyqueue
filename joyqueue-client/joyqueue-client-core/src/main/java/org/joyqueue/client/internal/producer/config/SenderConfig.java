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
package org.joyqueue.client.internal.producer.config;

/**
 * SenderConfig
 *
 * author: gaohaoxiang
 * date: 2019/2/11
 */
public class SenderConfig {

    private boolean compress = true;
    private int compressThreshold = 1024 * 1;
    private String compressType;
    private boolean batch;

    public SenderConfig() {

    }

    public SenderConfig(boolean compress, int compressThreshold, String compressType, boolean batch) {
        this.compress = compress;
        this.compressThreshold = compressThreshold;
        this.compressType = compressType;
        this.batch = batch;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public int getCompressThreshold() {
        return compressThreshold;
    }

    public void setCompressThreshold(int compressThreshold) {
        this.compressThreshold = compressThreshold;
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressType(String compressType) {
        this.compressType = compressType;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public boolean isBatch() {
        return batch;
    }
}