package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.model.DataCenterQuery;
import io.chubao.joyqueue.nsr.service.DataCenterService;

import java.util.List;

/**
 * CompositionDataCenterService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionDataCenterService implements DataCenterService {

    private CompositionConfig config;
    private DataCenterService igniteDataCenterService;
    private DataCenterService journalkeeperDataCenterService;

    public CompositionDataCenterService(CompositionConfig config, DataCenterService igniteDataCenterService,
                                        DataCenterService journalkeeperDataCenterService) {
        this.config = config;
        this.igniteDataCenterService = igniteDataCenterService;
        this.journalkeeperDataCenterService = journalkeeperDataCenterService;
    }

    @Override
    public DataCenter getById(String id) {
        return null;
    }

    @Override
    public DataCenter get(DataCenter model) {
        return null;
    }

    @Override
    public void addOrUpdate(DataCenter dataCenter) {

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
