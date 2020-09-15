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
package org.joyqueue.nsr.composition.service;

import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.ClusterInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

/**
 * CompositionClusterInternalService
 * author: gaohaoxiang
 * date: 2019/10/31
 */
public class CompositionClusterInternalService implements ClusterInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionClusterInternalService.class);

    private CompositionConfig config;
    private ClusterInternalService sourceClusterInternalService;
    private ClusterInternalService targetClusterInternalService;

    public CompositionClusterInternalService(CompositionConfig config, ClusterInternalService sourceClusterInternalService, ClusterInternalService targetClusterInternalService) {
        this.config = config;
        this.sourceClusterInternalService = sourceClusterInternalService;
        this.targetClusterInternalService = targetClusterInternalService;
    }

    @Override
    public String getCluster() {
        return targetClusterInternalService.getCluster();
    }

    @Override
    public String addNode(URI uri) {
        return targetClusterInternalService.addNode(uri);
    }

    @Override
    public String removeNode(URI uri) {
        return targetClusterInternalService.removeNode(uri);
    }

    @Override
    public String updateNodes(List<URI> uris) {
        return targetClusterInternalService.updateNodes(uris);
    }

    @Override
    public String execute(String command, List<String> args) {
        return targetClusterInternalService.execute(command, args);
    }
}