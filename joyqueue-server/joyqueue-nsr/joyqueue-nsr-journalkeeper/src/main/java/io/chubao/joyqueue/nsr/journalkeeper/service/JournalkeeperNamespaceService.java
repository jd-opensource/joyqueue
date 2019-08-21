package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.nsr.journalkeeper.converter.NamespaceConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.NamespaceRepository;
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