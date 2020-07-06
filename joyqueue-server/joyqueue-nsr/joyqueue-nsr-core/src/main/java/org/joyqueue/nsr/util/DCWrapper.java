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
package org.joyqueue.nsr.util;

import org.joyqueue.domain.DataCenter;
import org.joyqueue.nsr.NsrPlugins;
import org.joyqueue.toolkit.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据处理工具
 */
public class DCWrapper {
    private static final Logger LOG= LoggerFactory.getLogger(DCWrapper.class);
    public static String DC_URL_SEPARATOR = ";";
    public static String DC_TYPE_SEPARATOR = ":";
    private DataCenter dataCenter;
    private DCMatcher[] matchers;

    public boolean match(String ip) {
        boolean match = false;
        if (matchers != null) {
            for (DCMatcher matcher : matchers) {
                match = matcher.match(ip);
                if (match) {
                    break;
                }
            }

        }
        return match;
    }


    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public DCWrapper(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
        this.matchers = buildDCMatcher(dataCenter.getUrl());
    }


    protected DCMatcher[] buildDCMatcher(String urls) {
        List<DCMatcher> dcMatchers = new ArrayList<>();
        for (String uri : urls.split(DC_URL_SEPARATOR)) {
            URL url = URL.valueOf(uri);
            String type = uri.split(DC_TYPE_SEPARATOR)[0];
            NsrPlugins.DCMatchersPlugins.metas(type).forEach(matcher->{
                DCMatcher dcMatcher=null;
                try {
                    dcMatcher = matcher.getTarget().getClass().newInstance();
                }catch (Exception e){
                    LOG.info("New DC matcher instance exception {}",matcher.getTarget().getClass(),e);
                }
                if(dcMatcher!=null) {
                    dcMatcher.setUrl(url);
                    dcMatchers.add(dcMatcher);
                }
            });
        }

        return dcMatchers.toArray(new DCMatcher[dcMatchers.size()]);
    }


    public static void main(String[] args) throws Exception {
        DCWrapper dcWrapper = new DCWrapper(new DataCenter("aa","bb","cc","IPRANGE://?pattern=127.0.0.1-127.0.0.2"));
        System.out.println(dcWrapper);
    }
}
