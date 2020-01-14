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
package org.joyqueue.broker.index.model;


/**
 * Created by zhuduohui on 2018/9/17.
 */
public class IndexMetadataAndError {
    private long index;
    private String metadata;
    private short error;


    public IndexMetadataAndError(long index, String metadata, short error) {

        this.index = index;
        this.metadata = metadata;
        this.error = error;
    }

    public long getIndex() {
        return index;
    }

    public String getMetadata() {
        return metadata;
    }

    public short getError() {
        return error;
    }

    @Override
    public String toString() {
        return String.format("IndexMetadataAndError[%d,%s,%d]",index, metadata, error);
    }

}
