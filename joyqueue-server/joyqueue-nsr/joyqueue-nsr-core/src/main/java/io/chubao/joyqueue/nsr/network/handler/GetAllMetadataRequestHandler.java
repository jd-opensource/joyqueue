package io.chubao.joyqueue.nsr.network.handler;

import io.chubao.joyqueue.domain.AllMetadata;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.config.NameServiceConfig;
import io.chubao.joyqueue.nsr.network.NsrCommandHandler;
import io.chubao.joyqueue.nsr.network.codec.GetAllMetadataResponseCodec;
import io.chubao.joyqueue.nsr.network.command.GetAllMetadataRequest;
import io.chubao.joyqueue.nsr.network.command.GetAllMetadataResponse;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
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
                    Thread.currentThread().sleep(config.getAllMetadataCacheExpireTime());
                } catch (Exception e) {
                    logger.error("refresh cache exception", e);
                }
            }
        }, "joyqueue-allmetadata-cache-refresh");
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