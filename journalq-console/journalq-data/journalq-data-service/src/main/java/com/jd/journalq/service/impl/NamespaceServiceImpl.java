package com.jd.journalq.service.impl;

import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.query.QNamespace;
import com.jd.journalq.service.NamespaceService;
import com.jd.journalq.nsr.NameSpaceServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 命名空间服务实现
 * Created by chenyanying3 on 2018-11-18.
 */
@Service("namespaceService")
public class NamespaceServiceImpl implements NamespaceService {
    private final Logger logger = LoggerFactory.getLogger(NamespaceServiceImpl.class);

    @Autowired
    private NameSpaceServerService nameSpaceServerService;


    @Override
    public Namespace findByCode(String code) {
        return nameSpaceServerService.findByCode(code);
    }

    @Override
    public Namespace findById(String s) throws Exception {
        return nameSpaceServerService.findById(s);
    }

    @Override
    public PageResult<Namespace> findByQuery(QPageQuery<QNamespace> query) {
        try {
            return nameSpaceServerService.findByQuery(query);
        } catch (Exception e) {
            logger.error("findByQuery exception",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int add(Namespace model) {
        try {
            return nameSpaceServerService.add(model);
        } catch (Exception e) {
            throw new RuntimeException("add",e);
        }
    }

    @Override
    public int delete(Namespace model) {
        try {
            return nameSpaceServerService.delete(model);
        } catch (Exception e) {
            throw new RuntimeException("delete", e);
        }
    }

    @Override
    public int update(Namespace model) {
        try {
            return nameSpaceServerService.update(model);
        } catch (Exception e) {
            throw new RuntimeException("update",e);
        }
    }

    @Override
    public List<Namespace> findByQuery(QNamespace query) throws Exception {
        return nameSpaceServerService.findByQuery(query);
    }

}
