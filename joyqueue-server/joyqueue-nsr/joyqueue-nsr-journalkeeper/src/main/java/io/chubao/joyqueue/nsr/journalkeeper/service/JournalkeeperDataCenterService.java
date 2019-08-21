package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.nsr.journalkeeper.converter.DataCenterConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.DataCenterRepository;
import io.chubao.joyqueue.nsr.service.DataCenterService;

import java.util.List;

/**
 * JournalkeeperDataCenterService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperDataCenterService implements DataCenterService {

    private DataCenterRepository dataCenterRepository;

    public JournalkeeperDataCenterService(DataCenterRepository dataCenterRepository) {
        this.dataCenterRepository = dataCenterRepository;
    }

    @Override
    public List<DataCenter> getAll() {
        return DataCenterConverter.convert(dataCenterRepository.getAll());
    }

    @Override
    public DataCenter getById(String id) {
        return DataCenterConverter.convert(dataCenterRepository.getById(id));
    }

    @Override
    public DataCenter add(DataCenter dataCenter) {
        return DataCenterConverter.convert(dataCenterRepository.add(DataCenterConverter.convert(dataCenter)));
    }

    @Override
    public DataCenter update(DataCenter dataCenter) {
        return DataCenterConverter.convert(dataCenterRepository.update(DataCenterConverter.convert(dataCenter)));
    }

    @Override
    public void delete(String id) {
        dataCenterRepository.delete(id);
    }
}