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

import com.jd.joyqueue.registry.PathData;

import java.util.List;

/**
 * 集群事件
 */
public class ClusterEvent {

    private String path;
    private List<PathData> clusterState;

    public ClusterEvent(String path, List<PathData> clusterState) {
        this.path = path;
        this.clusterState = clusterState;
    }

    public List<PathData> getClusterState() {
        return clusterState;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ClusterEvent [path = " + path + ", clusterState = " + clusterState.toString() + "]";
    }

}
