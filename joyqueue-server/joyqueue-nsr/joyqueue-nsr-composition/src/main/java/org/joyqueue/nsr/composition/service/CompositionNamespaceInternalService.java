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

import org.joyqueue.domain.Namespace;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionNamespaceInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionNamespaceInternalService implements NamespaceInternalService {

    protected final Logger logger = LoggerFactory.getLogger(CompositionNamespaceInternalService.class);

    private CompositionConfig config;
    private NamespaceInternalService sourceNamespaceService;
    private NamespaceInternalService targetNamespaceService;

    public CompositionNamespaceInternalService(CompositionConfig config, NamespaceInternalService sourceNamespaceService,
                                               NamespaceInternalService targetNamespaceService) {
        this.config = config;
        this.sourceNamespaceService = sourceNamespaceService;
        this.targetNamespaceService = targetNamespaceService;
    }

    @Override
    public List<Namespace> getAll() {
        if (config.isReadSource()) {
            return sourceNamespaceService.getAll();
        } else {
            try {
                return targetNamespaceService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourceNamespaceService.getAll();
            }
        }
    }

    @Override
    public Namespace getByCode(String code) {
        if (config.isReadSource()) {
            return sourceNamespaceService.getByCode(code);
        } else {
            try {
                return targetNamespaceService.getByCode(code);
            } catch (Exception e) {
                logger.error("getByCode exception, code: {}", code, e);
                return sourceNamespaceService.getByCode(code);
            }
        }
    }

    @Override
    public Namespace getById(String id) {
        if (config.isReadSource()) {
            return sourceNamespaceService.getById(id);
        } else {
            try {
                return targetNamespaceService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return sourceNamespaceService.getById(id);
            }
        }
    }

    @Override
    public Namespace add(Namespace namespace) {
        Namespace result = null;
        if (config.isWriteSource()) {
            result = sourceNamespaceService.add(namespace);
        }
        if (config.isWriteTarget()) {
            try {
                targetNamespaceService.add(namespace);
            } catch (Exception e) {
                logger.error("add exception, params: {}", namespace, e);
            }
        }
        return result;
    }

    @Override
    public Namespace update(Namespace namespace) {
        Namespace result = null;
        if (config.isWriteSource()) {
            result = sourceNamespaceService.update(namespace);
        }
        if (config.isWriteTarget()) {
            try {
                targetNamespaceService.update(namespace);
            } catch (Exception e) {
                logger.error("update exception, params: {}", namespace, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteSource()) {
            sourceNamespaceService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetNamespaceService.delete(id);
            } catch (Exception e) {
                logger.error("delete exception, params: {}", id, e);
            }
        }
    }
}