/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.convert;

import io.chubao.joyqueue.model.domain.DataCenter;
import io.chubao.joyqueue.toolkit.URL;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/28.
 */
public class NsrDataCenterConverter extends Converter<DataCenter, io.chubao.joyqueue.domain.DataCenter> {
    @Override
    protected io.chubao.joyqueue.domain.DataCenter forward(DataCenter dataCenter) {
        io.chubao.joyqueue.domain.DataCenter nsrDataCenter = new io.chubao.joyqueue.domain.DataCenter();
        if (dataCenter.getMatchType() != null && dataCenter.getIps() != null) {
            String ips = dataCenter.getIps();
            String ipArray[] = ips.split(";");
            List<String> urlList = new ArrayList<>();
            for (String ip:ipArray) {
                String url = String.format("%s://?pattern=%s", dataCenter.getMatchType(), ip);
                urlList.add(url);
            }
            nsrDataCenter.setUrl(StringUtils.join(urlList,";"));
        }
        if (dataCenter.getCode() != null) {
            nsrDataCenter.setCode(dataCenter.getCode());
        }
        if (dataCenter.getName() != null) {
            nsrDataCenter.setName(dataCenter.getName());
        }
        if (dataCenter.getRegion() != null) {
            nsrDataCenter.setRegion(dataCenter.getRegion());
        }
        return nsrDataCenter;
    }

    @Override
    protected DataCenter backward(io.chubao.joyqueue.domain.DataCenter nsrDataCenter) {
        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(nsrDataCenter.getId());
        String[] array = nsrDataCenter.getUrl().split(";");
        List<String> ipList = new ArrayList<>();
        for (String urlStr:array) {
            URL url = URL.valueOf(urlStr);
            if (url != null) {
                dataCenter.setMatchType(url.getProtocol());
                ipList.add(url.getParameters().get("pattern"));
            }
        }
        dataCenter.setIps(StringUtils.join(ipList,";"));
        dataCenter.setCode(nsrDataCenter.getCode());
        dataCenter.setName(nsrDataCenter.getName());
        dataCenter.setRegion(nsrDataCenter.getRegion());
        return dataCenter;
    }
}
