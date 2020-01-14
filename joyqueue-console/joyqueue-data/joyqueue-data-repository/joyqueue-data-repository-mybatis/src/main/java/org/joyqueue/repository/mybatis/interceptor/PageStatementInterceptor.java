/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.repository.mybatis.interceptor;

import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.util.List;

/**
 * @author hujunliang
 * @version V1.0
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PageStatementInterceptor extends PageInterceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) invocation.getTarget();

        // 获取MappedStatement,Configuration对象
        MetaObject metaObject =
                MetaObject.forObject(handler, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String statement = mappedStatement.getId();
        if (!isPageSql(statement,metaObject.getValue("boundSql.parameterObject"))) {
            return invocation.proceed();
        }

        Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
        Executor executor = (Executor) metaObject.getValue("delegate.executor");

        // 获取分页参数
        BoundSql boundSql = handler.getBoundSql();
        QPageQuery pageQuery = (QPageQuery) boundSql.getParameterObject();
        String countStatement = buildCountStatement(statement);
        List<Integer> counts = executor.query(configuration.
                getMappedStatement(countStatement), pageQuery, RowBounds.DEFAULT, null);

        int count = 0;
        if (counts != null && !counts.isEmpty()) {
            count = counts.get(0) == null ? 0 : counts.get(0);
        }

        if (pageQuery.getPagination() == null) {
            pageQuery.setPagination(new Pagination());
        }
        pageQuery.getPagination().setTotalRecord(count);

        String sql = boundSql.getSql();
        if (logger.isDebugEnabled()) {
            logger.debug("raw SQL : " + sql);
        }

        if (sql == null || sql.isEmpty() || sql.contains(" limit ")) {
            return invocation.proceed();
        }

        String originalSql = (String) metaObject.getValue("delegate.boundSql.sql");
        metaObject.setValue("delegate.boundSql.sql",
                getLimitString(originalSql, pageQuery.getPagination().getStart(),
                        pageQuery.getPagination().getSize()));
        metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
        metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);

        if (logger.isDebugEnabled()) {
            logger.debug("pagination SQL : " + sql);
        }
        return invocation.proceed();
    }
    private String getLimitString(String sql, int offset, int limit) {
        StringBuilder stringBuilder = new StringBuilder(sql);
        stringBuilder.append(" limit ");
        if (offset > 0) {
            stringBuilder.append(offset).append(",").append(limit);
        } else {
            stringBuilder.append(limit);
        }
        return stringBuilder.toString();
    }
}
