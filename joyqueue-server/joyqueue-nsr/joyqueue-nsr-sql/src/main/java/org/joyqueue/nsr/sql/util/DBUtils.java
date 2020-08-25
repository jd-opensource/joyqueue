package org.joyqueue.nsr.sql.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DBUtils
 * author: gaohaoxiang
 * date: 2019/8/1
 */
public class DBUtils {

    public static String insert(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            fillParams(preparedStatement, params);
            preparedStatement.execute();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            while (generatedKeys.next()) {
                return generatedKeys.getString(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int update(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            fillParams(preparedStatement, params);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int delete(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            fillParams(preparedStatement, params);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, String>> query(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            fillParams(preparedStatement, params);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();

            List<Map<String, String>> result = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    row.put(metaData.getColumnName(i + 1), resultSet.getString(i + 1));
                }
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void fillParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}