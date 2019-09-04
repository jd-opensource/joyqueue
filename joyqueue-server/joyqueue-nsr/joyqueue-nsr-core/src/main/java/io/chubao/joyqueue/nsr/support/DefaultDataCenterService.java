package io.chubao.joyqueue.nsr.support;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.nsr.service.DataCenterService;
import io.chubao.joyqueue.nsr.service.internal.DataCenterInternalService;

import java.util.List;

/**
 * DefaultDataCenterService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultDataCenterService implements DataCenterService {

    private DataCenterInternalService dataCenterInternalService;

    public DefaultDataCenterService(DataCenterInternalService dataCenterInternalService) {
        this.dataCenterInternalService = dataCenterInternalService;
    }

    @Override
    public List<DataCenter> getAll() {
        return dataCenterInternalService.getAll();
    }

    @Override
    public DataCenter getById(String id) {
        return dataCenterInternalService.getById(id);
    }

    @Override
    public DataCenter add(DataCenter dataCenter) {
        return dataCenterInternalService.add(dataCenter);
    }

    @Override
    public DataCenter update(DataCenter dataCenter) {
        return dataCenterInternalService.update(dataCenter);
    }

    @Override
    public void delete(String id) {
        dataCenterInternalService.delete(id);
    }
}