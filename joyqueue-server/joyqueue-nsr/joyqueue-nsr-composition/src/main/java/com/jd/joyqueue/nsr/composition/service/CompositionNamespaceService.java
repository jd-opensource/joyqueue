package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.domain.Namespace;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.model.NamespaceQuery;
import com.jd.joyqueue.nsr.service.NamespaceService;

import java.util.List;

/**
 * CompositionNamespaceService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionNamespaceService implements NamespaceService {

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
        return null;
    }

    @Override
    public Namespace get(Namespace model) {
        return null;
    }

    @Override
    public void addOrUpdate(Namespace namespace) {

    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(Namespace model) {

    }

    @Override
    public List<Namespace> list() {
        return null;
    }

    @Override
    public List<Namespace> list(NamespaceQuery query) {
        return null;
    }

    @Override
    public PageResult<Namespace> pageQuery(QPageQuery<NamespaceQuery> pageQuery) {
        return null;
    }
}
