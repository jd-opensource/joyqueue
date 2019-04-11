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
package com.jd.journalq.registry;

/**
 * 数据节点
 *
 * @author hexiaofeng
 */
public class PathData {

    /**
     * 路径
     */
    private String path;
    /**
     * 数据
     */
    private byte[] data;

    /**
     * 版本
     */
    private int version = -1;

    public PathData(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public PathData(String path, byte[] data, int version) {
        this.path = path;
        this.data = data;
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathData pathData = (PathData) o;
        if (path != null ? !path.equals(pathData.path) : pathData.path != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
