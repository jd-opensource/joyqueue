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

import org.joyqueue.domain.AllMetadata;
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
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GetAllMetadataRequestHandler
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class GetAllMetadataRequestHandler implements NsrCommandHandler, PropertySupplierAware, Types, com.jd.laf.extension.Type<String> {

    protected static final Logger logger = LoggerFactory.getLogger(GetAllMetadataRequestHandler.class);

    private NameServiceConfig config;
    private NameService nameService;

    private volatile byte[] allMetadataCache;
    private Thread refreshCacheThread;

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.config = new NameServiceConfig(supplier);
    }

    @Override
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
        this.refreshCacheThread = new Thread(() -> {
            while (true) {
                try {
                    if (!config.getAllMetadataCacheEnable()) {
                        continue;
                    }
                    allMetadataCache = doGetAllMetadata();

                } catch (Exception e) {
                    logger.error("refresh cache exception", e);
                }finally {
                    try {
                        Thread.currentThread().sleep(config.getAllMetadataCacheExpireTime());
                    }catch (InterruptedException e){
                        logger.info("refresh cache  thread interrupted");
                    }
                }
            }
        }, "joyqueue-allmetadata-cache-refresh");
        this.refreshCacheThread.setDaemon(true);
        this.refreshCacheThread.start();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        GetAllMetadataRequest getAllMetadataRequest = (GetAllMetadataRequest) command.getPayload();
        byte[] response = null;

        if (config.getAllMetadataCacheEnable()) {
            if (allMetadataCache == null) {
                allMetadataCache = doGetAllMetadata();
            }
            response = allMetadataCache;
        } else {
            response = doGetAllMetadata();
        }

        GetAllMetadataResponse getAllMetadataResponse = new GetAllMetadataResponse();
        getAllMetadataResponse.setResponse(response);
        return new Command(getAllMetadataResponse);
    }

    protected byte[] doGetAllMetadata() {
        AllMetadata allMetadata = nameService.getAllMetadata();
        return GetAllMetadataResponseCodec.toJson(allMetadata);
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