package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.nsr.journalkeeper.converter.NamespaceConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.NamespaceRepository;
import io.chubao.joyqueue.nsr.service.internal.NamespaceInternalService;

import java.util.List;

/**
 * JournalkeeperNamespaceInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperNamespaceInternalService implements NamespaceInternalService {

    private NamespaceRepository namespaceRepository;

    public JournalkeeperNamespaceInternalService(NamespaceRepository namespaceRepository) {
        this.namespaceRepository = namespaceRepository;
    }

    @Override
    public List<Namespace> getAll() {
        return NamespaceConverter.convert(namespaceRepository.getAll());
    }

    @Override
    public Namespace getByCode(String code) {
        return NamespaceConverter.convert(namespaceRepository.getByCode(code));
    }

    @Override
    public Namespace getById(String id) {
        return NamespaceConverter.convert(namespaceRepository.getById(id));
    }

    @Override
    public Namespace add(Namespace namespace) {
        return NamespaceConverter.convert(namespaceRepository.add(NamespaceConverter.convert(namespace)));
    }

    @Override
    public Namespace update(Namespace namespace) {
        return NamespaceConverter.convert(namespaceRepository.update(NamespaceConverter.convert(namespace)));
    }

    @Override
    public void delete(String id) {
        namespaceRepository.deleteById(id);
    }
}