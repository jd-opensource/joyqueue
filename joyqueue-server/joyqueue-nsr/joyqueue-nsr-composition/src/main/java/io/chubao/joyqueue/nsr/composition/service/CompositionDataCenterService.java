package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.DataCenterQuery;
import io.chubao.joyqueue.nsr.service.DataCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionDataCenterService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionDataCenterService implements DataCenterService {

    protected final Logger logger = LoggerFactory.getLogger(CompositionDataCenterService.class);

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
        if (config.isReadIgnite()) {
            return igniteDataCenterService.getById(id);
        } else {
            return journalkeeperDataCenterService.getById(id);
        }
    }

    @Override
    public DataCenter get(DataCenter model) {
        if (config.isReadIgnite()) {
            return igniteDataCenterService.get(model);
        } else {
            return journalkeeperDataCenterService.get(model);
        }
    }

    @Override
    public void addOrUpdate(DataCenter dataCenter) {
        if (config.isWriteIgnite()) {
            igniteDataCenterService.addOrUpdate(dataCenter);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperDataCenterService.addOrUpdate(dataCenter);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", dataCenter, e);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        if (config.isWriteIgnite()) {
            igniteDataCenterService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperDataCenterService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(DataCenter model) {
        if (config.isWriteIgnite()) {
            igniteDataCenterService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperDataCenterService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<DataCenter> list() {
        if (config.isReadIgnite()) {
            return igniteDataCenterService.list();
        } else {
            return journalkeeperDataCenterService.list();
        }
    }

    @Override
    public List<DataCenter> list(DataCenterQuery query) {
        if (config.isReadIgnite()) {
            return igniteDataCenterService.list(query);
        } else {
            return journalkeeperDataCenterService.list(query);
        }
    }

    @Override
    public PageResult<DataCenter> pageQuery(QPageQuery<DataCenterQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteDataCenterService.pageQuery(pageQuery);
        } else {
            return journalkeeperDataCenterService.pageQuery(pageQuery);
        }
    }
}
