package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.DataCenterConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.DataCenterRepository;
import io.chubao.joyqueue.nsr.model.DataCenterQuery;
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
    public DataCenter getById(String id) {
        return DataCenterConverter.convert(dataCenterRepository.getById(id));
    }

    @Override
    public DataCenter get(DataCenter model) {
        return DataCenterConverter.convert(dataCenterRepository.getByCode(model.getCode()));
    }

    @Override
    public void addOrUpdate(DataCenter dataCenter) {
        dataCenterRepository.add(DataCenterConverter.convert(dataCenter));
    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(DataCenter model) {

    }

    @Override
    public List<DataCenter> list() {
        return null;
    }

    @Override
    public List<DataCenter> list(DataCenterQuery query) {
        return null;
    }

    @Override
    public PageResult<DataCenter> pageQuery(QPageQuery<DataCenterQuery> pageQuery) {
        return null;
    }
}