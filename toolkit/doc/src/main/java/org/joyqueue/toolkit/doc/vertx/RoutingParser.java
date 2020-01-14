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
package org.joyqueue.toolkit.doc.vertx;

import org.joyqueue.toolkit.doc.APIDoc;
import org.joyqueue.toolkit.doc.DocEntry;
import org.joyqueue.toolkit.doc.MultiHandlerMetaParser;
import com.jd.laf.web.vertx.config.RouteConfig;
import com.jd.laf.web.vertx.config.VertxConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * xml routing parser
 *
 **/
public class RoutingParser implements MultiHandlerMetaParser<APIDoc> {
    private static final  Logger logger= LoggerFactory.getLogger(RoutingParser.class);
    private Map<String, RouteConfig> routeCache;
    private String file;
    public RoutingParser(String file){
        this.file=file;
    }
    @Override
    public Map<String, Map<String, APIDoc>> parse() {
        Map<String,Map<String,APIDoc>> result=new HashMap(8);
        try {
          VertxConfig  vertxConfig= VertxConfig.Builder.build(file);
          if(routeCache==null){
              routeCache=new HashMap<>(vertxConfig.getRoutes().size());
          }
          for(RouteConfig c:vertxConfig.getRoutes()){
              routeCache.put(c.getPath(),c);
              if(c.getHandlers().size()>0){
                 String handler= c.getHandlers().get(0);
                 String[] serviceKeys=handler.split("\\.");
                 if(serviceKeys.length>=0){
                    Map<String,APIDoc> serviceDocs=result.get(serviceKeys[0]);
                    if(serviceDocs==null){
                        serviceDocs=new HashMap<>(8);
                        result.put(serviceKeys[0],serviceDocs);
                    }
                    APIDoc doc= new APIDoc();
                    doc.setPath(c.getPath());
                    doc.setId(c.getPath());
                    doc.setHttpMethod(c.getInherit().toUpperCase());
                    serviceDocs.put(serviceKeys[1],doc);
                 }
              }
          }
        }catch (Exception e){
            logger.info("parse routing file error",e);
        }
        return result;
    }

    @Override
    public DocEntry first(String path) {
        RouteConfig config=routeCache.get(path);
        if(config!=null&&config.getHandlers().size()>0){
           String handler= config.getHandlers().get(0);
           return parse(handler);
        }
        return null;
    }

    @Override
    public DocEntry last(String path) {
        RouteConfig config=routeCache.get(path);
        if(config!=null&&config.getHandlers().size()>1){
            String handler= config.getHandlers().get(1);
            return parse(handler);
        }
        return null;
    }


    /**
     *
     * parse service goup and method key
     * s
     **/
    private DocEntry parse(String handler){
        String[] serviceKeys=handler.split(".");
        if(serviceKeys.length>=2){
            DocEntry entry=new DocEntry();
            entry.setGroup(serviceKeys[0]);
            entry.setKey(serviceKeys[1]);
            return entry;
        }
        return null;
    }
}
