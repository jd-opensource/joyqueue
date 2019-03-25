package com.jd.journalq.repository.mybatis.interceptor;


import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询结果拦截器
 *
 * @author hujunliang
 * @version V1.0
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
public class PageResultInterceptor extends PageInterceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 目标对象转换
        ResultSetHandler resultSetHandler = (ResultSetHandler) invocation.getTarget();

        // 获取MappedStatement,Configuration对象
        MetaObject metaObject =
                MetaObject.forObject(resultSetHandler, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        String statement = mappedStatement.getId();
        if (!isPageSql(statement,metaObject.getValue("boundSql.parameterObject"))) {
            return invocation.proceed();
        }

        // 获取分页参数
        QPageQuery pageQuery = (QPageQuery) metaObject.getValue("boundSql.parameterObject");

        List<PageResult> result = new ArrayList<PageResult>(1);
        PageResult page = new PageResult();
        page.setPagination(pageQuery.getPagination());
        page.setResult((List) invocation.proceed());
        result.add(page);

        return result;
    }
}
