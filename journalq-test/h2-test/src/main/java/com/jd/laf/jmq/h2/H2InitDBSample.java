/**
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
package com.jd.laf.jmq.h2;

import org.h2.tools.RunScript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by chengzhiliang on 2019/3/18.
 */
public class H2InitDBSample {

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        InputStream in = H2InitDBSample.class.getClassLoader().getResourceAsStream("init-db.sql");
        if (in == null) {
            System.out.println("Please add the file script.sql to the classpath, package "
                    + H2InitDBSample.class.getPackage().getName());
        } else {
            Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/./" + H2ServerModelStarter.FILE_NAME , "sa" , "");
            RunScript.execute(conn, new InputStreamReader(in));
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("SELECT count(*) FROM message_retry");
            while (rs.next()) {
                System.out.println(rs.getInt(1));
            }
            rs.close();
            stat.close();
            conn.close();
        }

    }
}
