package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.nsr.journalkeeper.domain.BrokerDTO;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * BrokerConverter
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class BrokerConverter {

    public static BrokerDTO convert(Broker broker) {
        if (broker == null) {
            return null;
        }
        BrokerDTO brokerDTO = new BrokerDTO();
        brokerDTO.setId(Long.valueOf(broker.getId()));
        brokerDTO.setIp(broker.getIp());
        brokerDTO.setPort(broker.getPort());
        brokerDTO.setDataCenter(broker.getDataCenter());
        brokerDTO.setRetryType(broker.getRetryType());
        brokerDTO.setPermission(broker.getPermission().getName());
        return brokerDTO;
    }

    public static Broker convert(BrokerDTO brokerDTO) {
        if (brokerDTO == null) {
            return null;
        }
        Broker broker = new Broker();
        broker.setId(Integer.valueOf(String.valueOf(brokerDTO.getId())));
        broker.setIp(brokerDTO.getIp());
        broker.setPort(brokerDTO.getPort());
        broker.setDataCenter(brokerDTO.getDataCenter());
        broker.setRetryType(brokerDTO.getRetryType());
        broker.setPermission(Broker.PermissionEnum.value(brokerDTO.getPermission()));
        return broker;
    }

    public static List<Broker> convert(List<BrokerDTO> brokerDTOList) {
        if (CollectionUtils.isEmpty(brokerDTOList)) {
            return Collections.emptyList();
        }
        List<Broker> result = Lists.newArrayListWithCapacity(brokerDTOList.size());
        for (BrokerDTO brokerDTO : brokerDTOList) {
            result.add(convert(brokerDTO));
        }
        return result;
    }
}
