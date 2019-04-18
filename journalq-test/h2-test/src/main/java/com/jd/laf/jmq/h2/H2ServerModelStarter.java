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
