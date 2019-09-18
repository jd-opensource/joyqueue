package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.nsr.journalkeeper.domain.DataCenterDTO;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * DataCenterConverter
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class DataCenterConverter {

    public static DataCenterDTO convert(DataCenter dataCenter) {
        if (dataCenter == null) {
            return null;
        }
        DataCenterDTO dataCenterDTO = new DataCenterDTO();
        dataCenterDTO.setId(dataCenter.getId());
        dataCenterDTO.setCode(dataCenter.getCode());
        dataCenterDTO.setName(dataCenter.getName());
        dataCenterDTO.setRegion(dataCenter.getRegion());
        dataCenterDTO.setUrl(dataCenter.getUrl());
        return dataCenterDTO;
    }

    public static DataCenter convert(DataCenterDTO dataCenterDTO) {
        if (dataCenterDTO == null) {
            return null;
        }
        DataCenter dataCenter = new DataCenter();
        dataCenter.setRegion(dataCenterDTO.getRegion());
        dataCenter.setCode(dataCenterDTO.getCode());
        dataCenter.setName(dataCenterDTO.getName());
        dataCenter.setUrl(dataCenterDTO.getUrl());
        return dataCenter;
    }

    public static List<DataCenter> convert(List<DataCenterDTO> dataCenterDTOList) {
        if (CollectionUtils.isEmpty(dataCenterDTOList)) {
            return Collections.emptyList();
        }
        List<DataCenter> result = Lists.newArrayListWithCapacity(dataCenterDTOList.size());
        for (DataCenterDTO dataCenterDTO : dataCenterDTOList) {
            result.add(convert(dataCenterDTO));
        }
        return result;
    }
}