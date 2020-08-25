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
package org.joyqueue.nsr.sql.service;

import org.joyqueue.nsr.service.internal.ClusterInternalService;

import java.net.URI;
import java.util.List;

/**
 * SQLClusterInternalService
 * author: gaohaoxiang
 * date: 2019/10/31
 */
public class SQLClusterInternalService implements ClusterInternalService {

    @Override
    public String getCluster() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String addNode(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String removeNode(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String updateNodes(List<URI> uris) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String execute(String command, List<String> args) {
        throw new UnsupportedOperationException();
    }
}