package com.jd.journalq.convert;

import com.jd.journalq.model.domain.DataCenter;
import com.jd.journalq.toolkit.URL;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/28.
 */
public class NsrDataCenterConverter extends Converter<DataCenter, com.jd.journalq.common.domain.DataCenter> {
    @Override
    protected com.jd.journalq.common.domain.DataCenter forward(DataCenter dataCenter) {
        com.jd.journalq.common.domain.DataCenter nsrDataCenter = new com.jd.journalq.common.domain.DataCenter();
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
    protected DataCenter backward(com.jd.journalq.common.domain.DataCenter nsrDataCenter) {
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
