package com.jd.journalq.datasource;


import com.zaxxer.hikari.proxy.ConnectionProxy;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Created by hexiaofeng on 14-11-30.
 */
public class HikariDataSourceTest {




    public void testConnection(){
        DataSourceConfig config = new DataSourceConfig();
        config.setConnectionProperties("useUnicode=true;characterEncoding=UTF8;rewriteBatchedStatements=true;socketTimeout=6000;autoReconnectForPools=true");
        config.setUrl("jdbc:mysql://vm.mysql.com:3306/jka");
        config.setUser("jka");
        config.setPassword("jka");
        config.setDriver("com.mysql.jdbc.Driver");
        config.setIdleTimeout(60000);
        config.setConnectionTimeout(3000);
        config.setValidationQuery("select 1");
        config.setMinIdle(3);
        config.setMaxPoolSize(10);
        config.setType("HikariCP");

        boolean flag = true;
        try {
            Field  errField = ConnectionProxy.class.getDeclaredField("SQL_ERRORS");
            errField.setAccessible(true);
            Set<String> errs = (Set<String>)errField.get(null);
            System.out.println(errs);
            errs.add("HY000");

            System.out.println(errs);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        DataSource dataSource = DataSourceFactory.build(config);

        do {

            try {

                Connection connection = null;
                PreparedStatement pstmt = null;
                try {
                    connection = dataSource.getConnection();
                    pstmt = connection.prepareStatement("select host from kafka_broker  where id=2887");
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        System.out.println("rs:" + rs.getString(1));
                    }

                    pstmt = connection.prepareStatement("update kafka_broker set broker_id=1 where id=2887");
                    int num = pstmt.executeUpdate();
                    System.out.println("execute num:"+num);

                    //flag = false;
                }finally {
                    if(pstmt != null) {
                        pstmt.close();
                    }
                    if(connection != null) {
                        connection.close();
                    }
                }


            } catch (SQLException sqle) {
                System.out.println(sqle.getErrorCode()+";sqlstate:"+sqle.getSQLState()+";"+sqle.getMessage());
                sqle.printStackTrace();
                //flag = true;
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (flag);

    }

    public static void main(String[] args){
        HikariDataSourceTest test = new HikariDataSourceTest();
        test.testConnection();
    }

}
/**


 1290;sqlstate:HY000;The MySQL server is running with the --read-only option so it cannot execute this statement
 java.sql.SQLException: The MySQL server is running with the --read-only option so it cannot execute this statement
 at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:1073)
 at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:3603)
 at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:3535)
 at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:1989)
 at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2150)
 at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2626)
 at com.mysql.jdbc.PreparedStatement.executeInternal(PreparedStatement.java:2119)
 at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2415)
 at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2333)
 at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2318)
 at com.zaxxer.hikari.proxy.PreparedStatementProxy.executeUpdate(PreparedStatementProxy.java:61)
 at com.zaxxer.hikari.proxy.HikariPreparedStatementProxy.executeUpdate(HikariPreparedStatementProxy.java)
 at HikariDataSourceTest.testConnection(HikariDataSourceTest.java:60)
 at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
 at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 at java.lang.reflect.Method.invoke(Method.java:497)
 at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
 at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
 at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:44)
 at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
 at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
 at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
 at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
 at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
 at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
 at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
 at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
 at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
 at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
 at org.junit.runner.JUnitCore.run(JUnitCore.java:160)
 at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:74)
 at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:211)
 at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:67)
 at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
 at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 at java.lang.reflect.Method.invoke(Method.java:497)
 at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)
 */