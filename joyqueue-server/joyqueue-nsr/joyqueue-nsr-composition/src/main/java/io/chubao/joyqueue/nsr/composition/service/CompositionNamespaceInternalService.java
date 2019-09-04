package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.internal.NamespaceInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionNamespaceInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionNamespaceInternalService implements NamespaceInternalService {

    protected final Logger logger = LoggerFactory.getLogger(CompositionNamespaceInternalService.class);

    private CompositionConfig config;
    private NamespaceInternalService igniteNamespaceService;
    private NamespaceInternalService journalkeeperNamespaceService;

    public CompositionNamespaceInternalService(CompositionConfig config, NamespaceInternalService igniteNamespaceService,
                                               NamespaceInternalService journalkeeperNamespaceService) {
        this.config = config;
        this.igniteNamespaceService = igniteNamespaceService;
        this.journalkeeperNamespaceService = journalkeeperNamespaceService;
    }

    @Override
    public List<Namespace> getAll() {
        if (config.isReadIgnite()) {
            return igniteNamespaceService.getAll();
        } else {
            return journalkeeperNamespaceService.getAll();
        }
    }

    @Override
    public Namespace getByCode(String code) {
        if (config.isReadIgnite()) {
            return igniteNamespaceService.getByCode(code);
        } else {
            return journalkeeperNamespaceService.getByCode(code);
        }
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
    public Namespace add(Namespace namespace) {
        Namespace result = null;
        if (config.isWriteIgnite()) {
            result = igniteNamespaceService.add(namespace);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperNamespaceService.add(namespace);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", namespace, e);
            }
        }
        return result;
    }

    @Override
    public Namespace update(Namespace namespace) {
        Namespace result = null;
        if (config.isWriteIgnite()) {
            result = igniteNamespaceService.update(namespace);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperNamespaceService.update(namespace);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", namespace, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteIgnite()) {
            igniteNamespaceService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperNamespaceService.delete(id);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", id, e);
            }
        }
    }
}