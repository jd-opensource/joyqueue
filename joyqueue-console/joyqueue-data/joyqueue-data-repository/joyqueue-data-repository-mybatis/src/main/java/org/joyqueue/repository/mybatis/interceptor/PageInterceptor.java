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

import org.joyqueue.model.QPageQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Plugin;

import java.util.Properties;

/**
 * @author hujunliang
 * @version V1.0
 */
public abstract class PageInterceptor implements Interceptor {
    protected static final Log logger = LogFactory.getLog(PageInterceptor.class);

    public static final String PAGE_SQL_KEY_PREFIX = "pageSqlKeyPrefix";
    public static final String PAGE_SQL_KEY_SUFFIX = "pageSqlKeySuffix";
    public static final String COUNT_SQL_KEY_PREFIX = "countSqlKeyPrefix";
    public static final String COUNT_SQL_KEY_SUFFIX = "countSqlKeySuffix";

    public static final String DEFAULT_PAGE_SQL_PREFIX = "find";
    public static final String DEFAULT_PAGE_SQL_SUFFIX = "ByQuery";
    public static final String DEFAULT_COUNT_SQL_PREFIX = "find";
    public static final String DEFAULT_COUNT_SQL_SUFFIX = "CountByQuery";

    protected String pageSqlPrefix = DEFAULT_PAGE_SQL_PREFIX;
    protected String pageSqlSuffix = DEFAULT_PAGE_SQL_SUFFIX;
    protected String countSqlPrefix = DEFAULT_COUNT_SQL_PREFIX;
    protected String countSqlSuffix = DEFAULT_COUNT_SQL_SUFFIX;

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        if (properties != null) {
            String sqlId = properties.getProperty(PAGE_SQL_KEY_PREFIX);
            if (sqlId != null && !sqlId.trim().isEmpty()) {
                pageSqlPrefix = sqlId;
            }

            sqlId = properties.getProperty(PAGE_SQL_KEY_SUFFIX);
            if (sqlId != null && !sqlId.trim().isEmpty()) {
                pageSqlSuffix = sqlId;
            }

            sqlId = properties.getProperty(COUNT_SQL_KEY_PREFIX);
            if (sqlId != null && !sqlId.trim().isEmpty()) {
                countSqlPrefix = sqlId;
            }

            sqlId = properties.getProperty(COUNT_SQL_KEY_SUFFIX);
            if (sqlId != null && !sqlId.trim().isEmpty()) {
                countSqlSuffix = sqlId;
            }
        }
    }

    /**
     * 判断是否为分页Sql
     *     命名规范：findByQuery, findXxxByQuery, 前缀为find，后缀为ByQuery，后缀请不要含CountByQuery
     * @param statement
     * @param pageQuery
     * @return
     */
    protected boolean isPageSql(String statement,Object pageQuery) {
        if (!(pageQuery instanceof QPageQuery)) return false;
        if (null == statement) return false;
        //针对findByQuery，专门判定，加速执行
        if (statement.endsWith(pageSqlPrefix + pageSqlSuffix)) return true;
        if (statement.endsWith(countSqlPrefix + countSqlSuffix)) return false;

        //sql命名规范： findXxxByQuery
        String method = statement.substring(statement.lastIndexOf(".")+1, statement.length());
        return null!=method  && method.startsWith(pageSqlPrefix)
                && method.endsWith(pageSqlSuffix) && !method.endsWith(countSqlSuffix);
    }

    /**
     * 构建分页总数查询sql名
     *    命名规范：findCountByQuery，findXxxCountByQuery
     * @param statement
     * @return
     */
    protected String buildCountStatement(String statement) {
        int lastIndex = statement.lastIndexOf(".") + 1;
        String method = statement.substring(lastIndex, statement.length());
        return statement.substring(0, lastIndex) + countSqlPrefix +
                method.substring(pageSqlPrefix.length(), method.lastIndexOf(pageSqlSuffix)) + countSqlSuffix;
    }
}
