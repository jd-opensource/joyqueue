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
package org.joyqueue.nsr.network.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joyqueue.domain.AllMetadata;
import org.joyqueue.domain.Config;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Types;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.network.NsrCommandHandler;
import org.joyqueue.nsr.network.codec.GetAllMetadataResponseCodec;
import org.joyqueue.nsr.network.command.GetAllMetadataRequest;
import org.joyqueue.nsr.network.command.GetAllMetadataResponse;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * GetAllMetadataRequestHandler
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class GetAllMetadataRequestHandler implements NsrCommandHandler, PropertySupplierAware, Types, com.jd.laf.extension.Type<String> {

    protected static final Logger logger = LoggerFactory.getLogger(GetAllMetadataRequestHandler.class);

    private PropertySupplier supplier;
    private NameServiceConfig config;
    private NameService nameService;

    private volatile AllMetadata allMetadataCache;
    private volatile byte[] allMetadataCacheByte;
    private Thread refreshCacheThread;

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.supplier = supplier;
        this.config = new NameServiceConfig(supplier);
    }

    @Override
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
        this.refreshCacheThread = new Thread(() -> {
            while (true) {
                try {
                    if (config.getAllMetadataCacheEnable()) {
                        allMetadataCacheByte = doGetAllMetadata();
                    }
                    Thread.currentThread().sleep(config.getAllMetadataCacheExpireTime());
                } catch (Exception e) {
                    logger.error("refresh cache exception", e);
                }
            }
        }, "joyqueue-allmetadata-cache-refresh-thread");
        this.refreshCacheThread.setDaemon(true);
        this.refreshCacheThread.start();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        GetAllMetadataRequest getAllMetadataRequest = (GetAllMetadataRequest) command.getPayload();
        byte[] response = null;

        if (config.getAllMetadataCacheEnable()) {
            if (allMetadataCacheByte == null) {
                allMetadataCacheByte = doGetAllMetadata();
            }
            response = allMetadataCacheByte;
        } else {
            response = doGetAllMetadata();
        }

        GetAllMetadataResponse getAllMetadataResponse = new GetAllMetadataResponse();
        getAllMetadataResponse.setResponse(response);
        return new Command(getAllMetadataResponse);
    }

    protected byte[] doGetAllMetadata() {
        boolean isException = false;
        try {
            allMetadataCache = nameService.getAllMetadata();
        } catch (Exception e) {
            if (allMetadataCache == null) {
                throw e;
            }
            isException = true;
            logger.error("get all metadata exception", e);
        }

        if (isException || config.getAllMetadataRewriteEnable()) {
            try {
                allMetadataCache.setConfigs(mergeMemoryConfigs(allMetadataCache.getConfigs()));
            } catch (Exception e1) {
                throw e1;
            }
        }

        return GetAllMetadataResponseCodec.toJson(allMetadataCache);
    }

    protected List<Config> mergeMemoryConfigs(List<Config> configs) {
        Set<Config> result = Sets.newHashSet();
        result.addAll(configs);
        List<Property> properties = supplier.getProperties();
        for (Property property : properties) {
            if ("all".equals(property.getGroup())) {
                result.add(new Config(property.getGroup(), property.getKey(), String.valueOf(property.getValue())));
            }
        }
        return Lists.newArrayList(result);
    }

    @Override
    public String type() {
        return SERVER_TYPE;
    }

    @Override
    public int[] types() {
        return new int[] {NsrCommandType.NSR_GET_ALL_METADATA_REQUEST};
    }
}