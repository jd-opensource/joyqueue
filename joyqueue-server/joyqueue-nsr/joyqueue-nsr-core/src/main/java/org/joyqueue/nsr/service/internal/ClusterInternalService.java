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
package org.joyqueue.nsr.service.internal;

import java.net.URI;
import java.util.List;

/**
 * ClusterInternalService
 * author: gaohaoxiang
 * date: 2019/10/31
 */
public interface ClusterInternalService {

    /**
     * 返回集群信息
     * @return
     */
    String getCluster();

    /**
     * 添加集群
     * @param uri
     * @return
     */
    String addNode(URI uri);

    /**
     * 删除节点
     * @param uri
     * @return
     */
    String removeNode(URI uri);

    /**
     * 更新节点
     * @param uris
     * @return
     */
    String updateNodes(List<URI> uris);

    /**
     * 执行集群命令
     * @param command
     * @param args
     * @return
     */
    String execute(String command, List<String> args);
}