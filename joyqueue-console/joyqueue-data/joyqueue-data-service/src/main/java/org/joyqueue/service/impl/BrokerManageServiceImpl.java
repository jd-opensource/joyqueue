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
package org.joyqueue.service.impl;

import org.joyqueue.exception.ServiceException;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.monitor.RestResponse;
import org.joyqueue.other.HttpRestService;
import org.joyqueue.service.BrokerManageService;
import org.joyqueue.service.BrokerService;
import org.joyqueue.toolkit.io.Directory;
import org.joyqueue.util.UrlEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 *
 * Broker manage service
 *
 **/
@Service("brokerManageService")
public class BrokerManageServiceImpl implements BrokerManageService {

    @Autowired
    private HttpRestService httpRestService;
    @Autowired
    protected BrokerService brokerService;

    private static final Logger logger = LoggerFactory.getLogger(BrokerManageServiceImpl.class);
    @Override
    public Directory storeTreeView(int brokerId,boolean recursive) {
        try {
            Broker broker = brokerService.findById(brokerId);
            String path="storeTreeView";
            String[] args=new String[3];
            args[0]=broker.getIp();
            args[1]=String.valueOf(broker.getMonitorPort());
            args[2]=String.valueOf(recursive);
            RestResponse<Directory> restResponse = httpRestService.get(path,Directory.class,false,args);
            if (restResponse != null && restResponse.getData() != null) {
                Directory directory= restResponse.getData();
                return directory;
            }
        }catch (Exception e){
            logger.error("", e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR,e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean deleteGarbageFile(int brokerId, String fileName,boolean retain) {
        try {
            Broker broker = brokerService.findById(brokerId);
            String path="deleteGarbageFile";
            String[] args=new String[4];
            args[0]=broker.getIp();
            args[1]=String.valueOf(broker.getMonitorPort());
            args[2]= fileName;
            args[3]=String.valueOf(retain);
            RestResponse<Boolean> restResponse = httpRestService.delete(path,Boolean.class,false, UrlEncoderUtil.encodeParam(args));
            if (restResponse != null && restResponse.getData() != null) {
                return restResponse.getData();
            }
        }catch (Exception e){
            logger.error("", e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR,e.getMessage(), e);
        }
        return false;
    }
}
