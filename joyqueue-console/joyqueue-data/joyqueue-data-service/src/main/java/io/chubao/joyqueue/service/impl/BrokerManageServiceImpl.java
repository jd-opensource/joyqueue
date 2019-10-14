package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.manage.PartitionGroupMetric;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.monitor.RestResponse;
import io.chubao.joyqueue.other.HttpRestService;
import io.chubao.joyqueue.service.BrokerManageService;
import io.chubao.joyqueue.service.BrokerService;
import io.chubao.joyqueue.toolkit.io.Directory;
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
    @Override
    public Directory storeTreeView(int brokerId) {
        try {
            Broker broker = brokerService.findById(Long.valueOf(brokerId));
            String path="storeTreeView";
            String[] args=new String[2];
            args[0]=broker.getIp();
            args[1]=String.valueOf(broker.getMonitorPort());
            RestResponse<Directory> restResponse = httpRestService.get(path,Directory.class,false,args);
            if (restResponse != null && restResponse.getData() != null) {
                return restResponse.getData();
            }
        }catch (Exception e){
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR,e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteGarbageFile(int brokerId, String fileName) {
        try {
            Broker broker = brokerService.findById(Long.valueOf(brokerId));
            String path="deleteGarbageFile";
            String[] args=new String[3];
            args[0]=broker.getIp();
            args[1]=String.valueOf(broker.getMonitorPort());
            args[2]=fileName;
            RestResponse<Boolean> restResponse = httpRestService.delete(path,Boolean.class,false,args);
            if (restResponse != null && restResponse.getData() != null) {
                return restResponse.getData();
            }
        }catch (Exception e){
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR,e.getMessage());
        }
        return false;
    }
}
