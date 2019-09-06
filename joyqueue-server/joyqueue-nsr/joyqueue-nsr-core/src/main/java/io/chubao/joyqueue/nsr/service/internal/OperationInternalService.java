package io.chubao.joyqueue.nsr.service.internal;

import java.util.List;

/**
 * OperationInternalService
 * author: gaohaoxiang
 * date: 2019/9/6
 */
public interface OperationInternalService {

    Object query(String operator, List<Object> params);

    Object insert(String operator, List<Object> params);

    Object update(String operator, List<Object> params);

    Object delete(String operator, List<Object> params);
}