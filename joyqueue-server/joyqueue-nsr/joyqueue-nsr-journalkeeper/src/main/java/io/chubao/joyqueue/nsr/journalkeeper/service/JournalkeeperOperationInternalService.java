package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.nsr.journalkeeper.repository.BaseRepository;
import io.chubao.joyqueue.nsr.service.internal.OperationInternalService;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * JournalkeeperOperationInternalService
 * author: gaohaoxiang
 * date: 2019/9/6
 */
public class JournalkeeperOperationInternalService implements OperationInternalService {

    private BaseRepository baseRepository;

    public JournalkeeperOperationInternalService(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    @Override
    public Object query(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.query(operator);
        } else {
            return baseRepository.query(operator, params.toArray(new Object[0]));
        }
    }

    @Override
    public Object insert(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.insert(operator);
        } else {
            return baseRepository.insert(operator, params.toArray(new Object[0]));
        }
    }

    @Override
    public Object update(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.update(operator);
        } else {
            return baseRepository.update(operator, params.toArray(new Object[0]));
        }
    }

    @Override
    public Object delete(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.delete(operator);
        } else {
            return baseRepository.delete(operator, params.toArray(new Object[0]));
        }
    }
}