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
package org.joyqueue.nsr.sql.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.nsr.sql.domain.DataCenterDTO;

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