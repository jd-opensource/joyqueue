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
package com.jd.joyqueue.registry.memory;

/**
 * 节点事件
 * Created by hexiaofeng on 15-7-10.
 */
public class NodeEvent {
    // 类型
    protected NodeEventType type;
    // 父节点
    protected Node parent;
    // 当前节点
    protected Node node;

    public NodeEvent(NodeEventType type, Node parent, Node node) {
        this.type = type;
        this.parent = parent;
        this.node = node;
    }

    public NodeEventType getType() {
        return type;
    }

    public Node getParent() {
        return parent;
    }

    public Node getNode() {
        return node;
    }


    /**
     * 节点事件类型
     */
    public enum NodeEventType {
        /**
         * 更新
         */
        UPDATE,
        /**
         * 增加
         */
        ADD,
        /**
         * 删除
         */
        DELETE
    }
}
