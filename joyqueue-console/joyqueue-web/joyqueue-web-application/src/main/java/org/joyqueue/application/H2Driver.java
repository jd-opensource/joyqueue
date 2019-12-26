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
package org.joyqueue.application;

import org.h2.Driver;
import org.h2.jdbc.JdbcConnection;
import org.h2.upgrade.DbUpgrade;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by chenyanying3 on 19-4-13.
 */
public class H2Driver extends Driver {

    protected static final String DEFAULT_URL = "jdbc:default:connection";

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        try {
            //prepare connection
            if (info == null) {
                info = new Properties();
            }
            if (!acceptsURL(url)) {
                return null;
            }
            if (url.equals(DEFAULT_URL)) {
                return super.connect(url, info);
            }
            //connect
            Connection c = DbUpgrade.connectOrUpgrade(url, info);
            if (c != null) {
                return c;
            }
            return new JdbcConnection(url, info);
        } catch (Exception e) {
            //start h2 database server
            new H2DBServer().start(url);
            //reconnect
            return super.connect(url, info);
        }
    }
}
