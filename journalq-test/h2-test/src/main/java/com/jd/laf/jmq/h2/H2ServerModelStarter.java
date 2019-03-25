package com.jd.laf.jmq.h2;

import java.sql.SQLException;

/**
 * Created by chengzhiliang on 2019/3/18.
 */
public class H2ServerModelStarter {
    public static String FILE_NAME = "h2-db-jmq";

    public static void main(String[] args) throws SQLException {
        // tcp server 启动
        org.h2.tools.Server.main(new String[]{"-tcp", "-baseDir", "./" + FILE_NAME});

        // web console 启动
//        org.h2.tools.Server.main(new String[]{"-web", "-baseDir", "./" + FILE_NAME});

        System.out.println("H2 server is started.");
    }
}
