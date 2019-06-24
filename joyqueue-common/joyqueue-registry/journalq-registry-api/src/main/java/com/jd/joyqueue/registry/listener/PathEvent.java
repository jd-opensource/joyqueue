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
 * 路径事件
 */
public class PathEvent {

    private PathEventType type;
    private String path;
    private byte[] data;
    private int version = -1;

    public PathEvent(PathEventType type, String path, byte[] data) {
        this.type = type;
        this.path = path;
        this.data = data;
    }

    public PathEvent(PathEventType type, String path, byte[] data, int version) {
        this.type = type;
        this.path = path;
        this.data = data;
        this.version = version;
    }

    /**
     * @return 事件类型
     */
    public PathEventType getType() {
        return type;
    }

    /**
     * @return 全路径
     */
    public String getPath() {
        return path;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PathEvent [type=" + type + ", path=" + path + ", data=" + ((data == null || data.length < 1) ? "" :
                new String(
                data)) + "]";
    }

    public enum PathEventType {
        CREATED, REMOVED, UPDATED
    }

}
