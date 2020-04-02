/**
 * Partially copied from Apache Kafka.
 *
 * Original LICENSE :
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.toolkit.delay;

/**
 * Created by zhangkepeng on 17-2-13.
 */
public class DelayedOperationKey {

    private String keyLabel;

    public DelayedOperationKey(Object... args) {
        StringBuilder builder = new StringBuilder();
        if (args != null) {
            int size = args.length;
            for (int i = 0; i < size; i++) {
                if (i == size - 1) {
                    builder.append(args[i].toString());
                } else {
                    builder.append(args[i].toString() + "-");
                }
            }
        }
        keyLabel = builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DelayedOperationKey that = (DelayedOperationKey) o;

        if (keyLabel != null ? !keyLabel.equals(that.keyLabel) : that.keyLabel != null) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public int hashCode() {
        int result = keyLabel != null ? keyLabel.hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return String.format("delayed operation key: " + keyLabel);
    }

    public String getKeyLabel() {
        return keyLabel;
    }
}
