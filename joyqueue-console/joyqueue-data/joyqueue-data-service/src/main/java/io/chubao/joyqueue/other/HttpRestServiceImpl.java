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
package io.chubao.joyqueue.other;

import io.chubao.joyqueue.monitor.RestResponse;
import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.service.BrokerRestUrlMappingService;
import io.chubao.joyqueue.util.HttpUtil;
import io.chubao.joyqueue.util.JSONParser;
import io.chubao.joyqueue.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("httpRestService")
public class HttpRestServiceImpl implements HttpRestService {
    private static final Logger logger= LoggerFactory.getLogger(HttpRestServiceImpl.class);

    @Autowired
    private BrokerRestUrlMappingService urlMappingService;
    @Override
    public <T> RestResponse<T> get(String pathKey, Class dataClass, boolean isList, String... args) {
        String urlTemplate= urlMappingService.urlTemplate(pathKey);
        String url;
        if(!NullUtil.isEmpty(args)){
           //args= UrlEncoderUtil.encodeParam(args);
           url= String.format(urlTemplate,args);
       }else{
           url=urlTemplate;
        }
        try {
            logger.info("http request:"+url);
            String responseString = HttpUtil.get(url);
           return JSONParser.parse(responseString, RestResponse.class, dataClass, isList);
        }catch (ServiceException e){
            logger.info("proxy monitor exception",e);
            throw e;
        }catch (Exception e){
            throw new ServiceException(ServiceException.IO_ERROR, e.getMessage());
        }
    }

    @Override
    public <T> RestResponse<T> put(String pathKey, Class dataClass, boolean isList, String content, String... args) {
        String urlTemplate= urlMappingService.urlTemplate(pathKey);
        String url;
        if(!NullUtil.isEmpty(args)){
            //args= UrlEncoderUtil.encodeParam(args);
            url= String.format(urlTemplate,args);
        }else{
            url=urlTemplate;
        }
        try {
            logger.info("http request:"+url);
            String responseString = HttpUtil.put(url,content);
            return JSONParser.parse(responseString, RestResponse.class, dataClass, isList);
        }catch (ServiceException e){
            logger.info("proxy monitor exception",e);
            throw e;
        }catch (Exception e){
            throw new ServiceException(ServiceException.IO_ERROR, e.getMessage());
        }
    }

    @Override
    public <T> RestResponse<T> delete(String pathKey, Class dataClass, boolean isList, String... args) {
        String urlTemplate= urlMappingService.urlTemplate(pathKey);
        String url;
        if(!NullUtil.isEmpty(args)){
            //args= UrlEncoderUtil.encodeParam(args);
            url= String.format(urlTemplate,args);
        }else{
            url=urlTemplate;
        }
        try {
            logger.info("http request:"+url);
            String responseString = HttpUtil.delete(url);
            return JSONParser.parse(responseString, RestResponse.class, dataClass, isList);
        }catch (ServiceException e){
            logger.info("proxy monitor exception",e);
            throw e;
        }catch (Exception e){
            throw new ServiceException(ServiceException.IO_ERROR, e.getMessage());
        }
    }
}
