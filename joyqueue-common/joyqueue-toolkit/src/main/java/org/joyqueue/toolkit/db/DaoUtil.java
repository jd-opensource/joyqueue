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
package org.joyqueue.toolkit.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Dao工具类
 */
public class DaoUtil {

    /**
     * 插入
     *
     * @param dataSource 数据源
     * @param targets    对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 插入记录条数
     * @throws Exception
     */
    public static <T> int insert(final DataSource dataSource, final List<T> targets, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (targets == null || targets.isEmpty()) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can not be null.");
        }

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            int count = insert(connection, targets, sql, callback);
            connection.commit();
            return count;
        } finally {
            close(connection, null, null);
        }
    }


    /**
     * 插入，外面控制事务提交和连接关闭，出错会自动回滚
     *
     * @param connection 连接
     * @param targets    对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 插入记录条数
     * @throws Exception
     */
    public static <T> int insert(final Connection connection, final List<T> targets, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (targets == null || targets.isEmpty()) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null.");
        }

        PreparedStatement statement = null;
        ResultSet rs = null;
        InsertCallback<T> insertCallback = null;
        if (callback instanceof InsertCallback) {
            insertCallback = (InsertCallback<T>) callback;
        }
        try {
            statement = connection.prepareStatement(sql,
                    insertCallback == null ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS);
            int count = 0;
            for (T target : targets) {
                if (target != null) {
                    callback.before(statement, target);
                    count += statement.executeUpdate();
                    if (insertCallback != null) {
                        rs = statement.getGeneratedKeys();
                        if (rs.next()) {
                            insertCallback.after(rs, target);
                        }
                        rs.close();
                        rs = null;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            close(null, statement, rs);
        }
    }

    /**
     * 插入
     *
     * @param dataSource 数据源
     * @param target     对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 插入记录条数
     * @throws Exception
     */
    public static <T> int insert(final DataSource dataSource, final T target, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (target == null) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can not be null.");
        }

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            return insert(connection, target, sql, callback);
        } finally {
            close(connection, null, null);
        }
    }


    /**
     * 插入，外面控制事务提交和连接关闭，出错会自动回滚
     *
     * @param connection 连接
     * @param target     对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 插入记录条数
     * @throws Exception
     */
    public static <T> int insert(final Connection connection, final T target, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (target == null) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null.");
        }
        PreparedStatement statement = null;
        ResultSet rs = null;
        InsertCallback<T> insertCallback = null;
        if (callback instanceof InsertCallback) {
            insertCallback = (InsertCallback<T>) callback;
        }
        try {
            statement = connection.prepareStatement(sql,
                    insertCallback == null ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS);
            callback.before(statement, target);
            int count = statement.executeUpdate();
            if (insertCallback != null) {
                rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    insertCallback.after(rs, target);
                }
            }
            return count;
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            close(null, statement, rs);
        }
    }

    /**
     * 更新
     *
     * @param dataSource 数据源
     * @param target     对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 更新记录条数
     * @throws Exception
     */
    public static <T> int update(final DataSource dataSource, final T target, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (target == null) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can not be null.");
        }

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            return update(connection, target, sql, callback);
        } finally {
            close(connection, null, null);
        }
    }

    /**
     * 更新，外面控制事务提交和连接关闭，出错会自动回滚
     *
     * @param connection 连接
     * @param target     对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 更新记录条数
     * @throws Exception
     */
    public static <T> int update(final Connection connection, final T target, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (target == null) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null.");
        }

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            callback.before(statement, target);
            return statement.executeUpdate();
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            close(null, statement, null);
        }
    }

    /**
     * 更新
     *
     * @param dataSource 数据源
     * @param targets    对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 更新记录条数
     * @throws Exception
     */
    public static <T> int update(final DataSource dataSource, final List<T> targets, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (targets == null || targets.isEmpty()) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can not be null.");
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            int count = update(connection, targets, sql, callback);
            connection.commit();
            return count;
        } finally {
            close(connection, null, null);
        }
    }

    /**
     * 更新，外面控制事务提交和连接关闭，出错会自动回滚
     *
     * @param connection 连接
     * @param targets    对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 更新记录条数
     * @throws Exception
     */
    public static <T> int update(final Connection connection, final List<T> targets, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (targets == null || targets.isEmpty()) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null.");
        }
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            int count = 0;
            for (T target : targets) {
                if (target != null) {
                    callback.before(statement, target);
                    count += statement.executeUpdate();
                }
            }
            return count;
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            close(null, statement, null);
        }
    }

    /**
     * 删除
     *
     * @param dataSource 数据源
     * @param target     对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 删除记录条数
     * @throws Exception
     */
    public static <T> int delete(final DataSource dataSource, final T target, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (target == null) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can not be null.");
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            return delete(connection, target, sql, callback);
        } finally {
            close(connection, null, null);
        }
    }

    /**
     * 删除，外面控制事务提交和连接关闭，出错会自动回滚
     *
     * @param connection 连接
     * @param target     对象
     * @param sql        SQL
     * @param callback   回调函数
     * @param <T>        类型
     * @return 删除记录条数
     * @throws Exception
     */
    public static <T> int delete(final Connection connection, final T target, final String sql,
                                 final UpdateCallback<T> callback) throws Exception {
        if (target == null) {
            return 0;
        }
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null.");
        }
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            callback.before(statement, target);
            return statement.executeUpdate();
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            close(null, statement, null);
        }
    }

    /**
     * 回滚，不抛出异常
     *
     * @param connection 连接
     */
    protected static void rollback(final Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            // 自动提交模式，不需要回滚，否则会报错
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (SQLException e) {
        }
    }

    /**
     * 执行
     *
     * @param dataSource 数据源
     * @param sql        SQL
     * @param callback   回调
     * @return 影响记录条数
     * @throws Exception
     */
    public static int execute(final DataSource dataSource, final String sql, final StatementCallback callback) throws
            Exception {
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can not be null.");
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            return execute(connection, sql, callback);
        } finally {
            close(connection, null, null);
        }
    }

    /**
     * 执行，外面控制事务提交和连接关闭，出错会自动回滚
     *
     * @param connection 连接
     * @param sql        SQL
     * @param callback   回调
     * @return 影响记录条数
     * @throws Exception
     */
    public static int execute(final Connection connection, final String sql, final StatementCallback callback) throws
            Exception {
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("sql can not be empty.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null.");
        }
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            callback.before(statement);
            return statement.executeUpdate();
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            close(null, statement, null);
        }
    }

    /**
     * 查询对象
     *
     * @param dataSource 数据源
     * @param sql        SQL
     * @param callback   回调
     * @param <T>        类型
     * @return 结果对象
     * @throws Exception
     */
    public static <T> T queryObject(final DataSource dataSource, final String sql,
                                    final QueryCallback<T> callback) throws Exception {
        if (dataSource == null || sql == null || sql.isEmpty() || callback == null) {
            return null;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            callback.before(statement);
            rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return callback.map(rs);
        } finally {
            close(connection, statement, rs);
        }
    }

    /**
     * 查询对象
     *
     * @param dbConfig 数据源
     * @param sql      SQL
     * @param callback 回调
     * @param <T>      类型
     * @return 结果对象
     * @throws Exception
     */
    public static <T> T queryObject(final DBConfig dbConfig, final String sql, final QueryCallback<T> callback) throws
            Exception {
        if (dbConfig == null || sql == null || sql.isEmpty() || callback == null) {
            return null;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection =
                    DriverManager.getConnection(dbConfig.jdbcUrl(), dbConfig.getUsername(), dbConfig.getPassword());
            statement = connection.prepareStatement(sql);
            callback.before(statement);
            rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return callback.map(rs);
        } finally {
            close(connection, statement, rs);
        }
    }

    /**
     * 关闭连接
     *
     * @param connection 连接
     * @param statement  声明
     * @param resultSet  结果集合
     */
    public static void close(final Connection connection, final Statement statement, final ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * 查询列表
     *
     * @param dataSource 数据源
     * @param sql        SQL
     * @param callback   回调
     * @param <T>        类型
     * @return 结果列表
     * @throws Exception
     */
    public static <T> List<T> queryList(final DataSource dataSource, final String sql,
                                        final QueryCallback<T> callback) throws Exception {
        List<T> result = new ArrayList<T>();
        if (dataSource == null || sql == null || sql.isEmpty() || callback == null) {
            return result;
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            callback.before(statement);
            rs = statement.executeQuery();
            while (rs.next()) {
                result.add(callback.map(rs));
            }
            return result;
        } finally {
            close(connection, statement, rs);
        }
    }


    /**
     * 查询列表
     *
     * @param dbConfig 连接配置
     * @param sql      SQL
     * @param callback 回调
     * @param <T>      类型
     * @return 结果列表
     * @throws Exception
     */
    public static <T> List<T> queryList(final DBConfig dbConfig, final String sql,
                                        final QueryCallback<T> callback) throws Exception {
        List<T> result = new ArrayList<T>();
        if (dbConfig == null || sql == null || sql.isEmpty() || callback == null) {
            return result;
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection =
                    DriverManager.getConnection(dbConfig.jdbcUrl(), dbConfig.getUsername(), dbConfig.getPassword());
            statement = connection.prepareStatement(sql);
            callback.before(statement);
            rs = statement.executeQuery();
            while (rs.next()) {
                result.add(callback.map(rs));
            }
            return result;
        } finally {
            close(connection, statement, rs);
        }
    }

    /**
     * 声明回调
     */
    public interface StatementCallback {

        void before(PreparedStatement statement) throws Exception;
    }

    /**
     * 更新声明回调
     */
    public interface UpdateCallback<T> {

        /**
         * 插入之前
         *
         * @param statement 语句
         * @param target    目标对象
         * @throws Exception
         */
        void before(PreparedStatement statement, T target) throws Exception;

    }

    /**
     * 插入声明回调
     */
    public interface InsertCallback<T> extends UpdateCallback<T> {

        /**
         * 插入之后
         *
         * @param rs     主键数据集
         * @param target 目标对象
         * @throws Exception
         */
        void after(ResultSet rs, T target) throws Exception;
    }

    /**
     * 查询回调
     */
    public interface QueryCallback<T> extends StatementCallback {

        /**
         * 创建对象
         *
         * @param rs 记录集
         * @return 对象
         * @throws Exception
         */
        T map(ResultSet rs) throws Exception;
    }


    /**
     * 数据库配置
     */
    public static class DBConfig {
        // 驱动程序
        private String driverClassName;
        // URL
        private String url;
        // 用户
        private String username;
        // 密码
        private String password;
        // 连接配置属性
        private String connectionProperties;

        public DBConfig() {
        }

        public DBConfig(String driverClassName, String url, String username, String password,
                        String connectionProperties) {
            this.driverClassName = driverClassName;
            this.url = url;
            this.username = username;
            this.password = password;
            this.connectionProperties = connectionProperties;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConnectionProperties() {
            return connectionProperties;
        }

        public void setConnectionProperties(String connectionProperties) {
            this.connectionProperties = connectionProperties;
        }

        public String jdbcUrl() {
            if (url == null || url.isEmpty()) {
                return null;
            }
            if (connectionProperties == null || connectionProperties.isEmpty()) {
                return url;
            }
            int pos = url.indexOf('?');
            StringBuilder sb = new StringBuilder(100);
            sb.append(url.trim());

            int count = 0;
            StringTokenizer tokenizer = new StringTokenizer(connectionProperties.trim(), ";");
            while (tokenizer.hasMoreTokens()) {
                sb.append(count++ == 0 && pos == -1 ? '?' : '&').append(tokenizer.nextToken());
            }
            return sb.toString();
        }
    }


}
