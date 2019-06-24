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
package com.jd.joyqueue.registry.listener;

/**
 * 孩子变化事件
 */
public class ChildrenEvent {

    private ChildrenEventType type;
    private String path;
    private byte[] data;

    public ChildrenEvent(ChildrenEventType type, String path, byte[] data) {
        this.type = type;
        this.path = path;
        this.data = data;
    }

    /**
     * @return 事件类型
     */
    public ChildrenEventType getType() {
        return type;
    }

    /**
     * @return 子节点全路径
     */
    public String getPath() {
        return path;
    }

    /**
     * @return 子节点数据
     */
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ChildrenEvent [type=" + type + ", path=" + path + ", data=" + ((data == null || data.length < 1) ? ""
                : new String(
                data)) + "]";
    }

    public enum ChildrenEventType {
        /**
         * 增加
         */
        CHILD_CREATED,
        /**
         * 删除
         */
        CHILD_REMOVED,
        /**
         * 修改
         */
        CHILD_UPDATED
    }

}
