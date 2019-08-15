package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.NamespaceQuery;
import io.chubao.joyqueue.nsr.service.NamespaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionNamespaceService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionNamespaceService implements NamespaceService {

    protected final Logger logger = LoggerFactory.getLogger(CompositionNamespaceService.class);

    private CompositionConfig config;
    private NamespaceService igniteNamespaceService;
    private NamespaceService journalkeeperNamespaceService;

    public CompositionNamespaceService(CompositionConfig config, NamespaceService igniteNamespaceService,
                                       NamespaceService journalkeeperNamespaceService) {
        this.config = config;
        this.igniteNamespaceService = igniteNamespaceService;
        this.journalkeeperNamespaceService = journalkeeperNamespaceService;
    }

    @Override
    public Namespace getById(String id) {
        if (config.isReadIgnite()) {
            return igniteNamespaceService.getById(id);
        } else {
            return journalkeeperNamespaceService.getById(id);
        }
    }

    @Override
    public Namespace get(Namespace model) {
        if (config.isReadIgnite()) {
            return igniteNamespaceService.get(model);
        } else {
            return journalkeeperNamespaceService.get(model);
        }
    }

    @Override
    public void addOrUpdate(Namespace namespace) {
        if (config.isWriteIgnite()) {
            igniteNamespaceService.addOrUpdate(namespace);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperNamespaceService.addOrUpdate(namespace);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", namespace, e);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        if (config.isWriteIgnite()) {
            igniteNamespaceService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperNamespaceService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(Namespace model) {
        if (config.isWriteIgnite()) {
            igniteNamespaceService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperNamespaceService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<Namespace> list() {
        if (config.isReadIgnite()) {
            return igniteNamespaceService.list();
        } else {
            return journalkeeperNamespaceService.list();
        }
    }

    @Override
    public List<Namespace> list(NamespaceQuery query) {
        if (config.isReadIgnite()) {
            return igniteNamespaceService.list(query);
        } else {
            return journalkeeperNamespaceService.list(query);
        }
    }

    @Override
    public PageResult<Namespace> pageQuery(QPageQuery<NamespaceQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteNamespaceService.pageQuery(pageQuery);
        } else {
            return journalkeeperNamespaceService.pageQuery(pageQuery);
        }
    }
}
