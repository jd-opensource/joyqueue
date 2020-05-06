/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.msg.filter.s3;

import org.joyqueue.msg.filter.FilterResponse;
import org.joyqueue.msg.filter.OutputType;
import org.joyqueue.msg.filter.TopicMsgFilterOutput;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class TopicMsgFilterS3Output implements TopicMsgFilterOutput {

    private S3Manager s3Manager;
    private static final Object mutex = new Object();

    @Override
    public FilterResponse output(String path) {
        return new FilterResponse(getS3Manager().upload(path), OutputType.S3);
    }

    private S3Manager getS3Manager() {
        if (s3Manager == null) {
            synchronized (mutex) {
                if (s3Manager == null) {
                    s3Manager = new S3Manager();
                }
            }
        }
        return s3Manager;
    }
}
