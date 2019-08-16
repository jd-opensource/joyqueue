package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.NamespaceConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.NamespaceRepository;
import io.chubao.joyqueue.nsr.model.NamespaceQuery;
import io.chubao.joyqueue.nsr.service.NamespaceService;

import java.util.List;

/**
 * JournalkeeperNamespaceService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperNamespaceService implements NamespaceService {

    private NamespaceRepository namespaceRepository;

    public JournalkeeperNamespaceService(NamespaceRepository namespaceRepository) {
        this.namespaceRepository = namespaceRepository;
    }

    @Override
    public Namespace getById(String id) {
        return NamespaceConverter.convert(namespaceRepository.getById(id));
    }

    @Override
    public Namespace get(Namespace model) {
        return NamespaceConverter.convert(namespaceRepository.getByCode(model.getCode()));
    }

    @Override
    public void addOrUpdate(Namespace namespace) {
        namespaceRepository.add(NamespaceConverter.convert(namespace));
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