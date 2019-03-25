package com.jd.journalq.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.ApiVersionsRequest;
import com.jd.journalq.broker.kafka.model.ApiVersion;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.broker.kafka.command.ApiVersionsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * ApiVersionsHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ApiVersionsHandler extends AbstractKafkaCommandHandler {

    protected static final Logger logger = LoggerFactory.getLogger(ApiVersionsHandler.class);

    private static final List<ApiVersion> APIS;
    private static final ApiVersionsResponse SUPPORTED_RESPONSE;

    static {
        List<ApiVersion> apis = Lists.newLinkedList();
        for (KafkaCommandType command : KafkaCommandType.values()) {
            if (command.isExport()) {
                apis.add(new ApiVersion(command.getCode(), command.getMinVersion(), command.getMaxVersion()));
            }
        }
        APIS = Collections.unmodifiableList(apis);
        SUPPORTED_RESPONSE = new ApiVersionsResponse(KafkaErrorCode.NONE, APIS);
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ApiVersionsRequest apiVersionsRequest = (ApiVersionsRequest) command.getPayload();

        // TODO 临时日志
        logger.debug("fetch api version, transport: {}, version: {}", transport.remoteAddress(), apiVersionsRequest.getVersion());
        return new Command(SUPPORTED_RESPONSE);
    }

    @Override
    public int type() {
        return KafkaCommandType.API_VERSIONS.getCode();
    }
}