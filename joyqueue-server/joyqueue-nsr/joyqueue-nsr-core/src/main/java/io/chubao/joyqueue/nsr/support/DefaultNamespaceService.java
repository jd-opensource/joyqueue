package io.chubao.joyqueue.nsr.support;

import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.nsr.service.NamespaceService;
import io.chubao.joyqueue.nsr.service.internal.NamespaceInternalService;

import java.util.List;

/**
 * DefaultNamespaceService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultNamespaceService implements NamespaceService {

    private NamespaceInternalService namespaceInternalService;

    public DefaultNamespaceService(NamespaceInternalService namespaceInternalService) {
        this.namespaceInternalService = namespaceInternalService;
    }

    @Override
    public List<Namespace> getAll() {
        return namespaceInternalService.getAll();
    }

    @Override
    public Namespace getByCode(String code) {
        return namespaceInternalService.getByCode(code);
    }

    @Override
    public Namespace getById(String id) {
        return namespaceInternalService.getById(id);
    }

    @Override
    public Namespace add(Namespace namespace) {
        return namespaceInternalService.add(namespace);
    }

    @Override
    public Namespace update(Namespace namespace) {
        return namespaceInternalService.update(namespace);
    }

    @Override
    public void delete(String id) {
        namespaceInternalService.delete(id);
    }
}