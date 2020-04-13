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
package org.joyqueue.hbase;

import org.apache.hadoop.hbase.client.*;
import org.joyqueue.toolkit.lang.Close;
import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.lang.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * HBASE客户端
 * <p>
 * Created by chengzhiliang on 2018/12/6.
 */
public class HBaseClient implements LifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(HBaseClient.class);

    public Configuration config = HBaseConfiguration.create();

    public Connection conn = null;

//    private final String nameSpace = "journalq";

    private String hBaseConfigPath = "hBase-client-config.xml";

    private boolean isStart = false;

    public HBaseClient() {
    }

    public HBaseClient(String hBaseConfigPath) {
        this.hBaseConfigPath = hBaseConfigPath;
    }

    @Override
    public void start() throws Exception{
        config.addResource(hBaseConfigPath);
        // 建立连接
        try {
            conn = ConnectionFactory.createConnection(config);
            isStart = true;
            logger.info("HBaseClient is started.");
        } catch (Exception e) {
            isStart = false;
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean isStarted() {
        return isStart;
    }

    @Override
    public void stop() {
        Close.close(conn);
    }

    public void put(String nameSpace, String tableName, byte[] cf, byte[] col, List<Pair<byte[], byte[]>> pairList) throws IOException {
        Table table = conn.getTable(TableName.valueOf(nameSpace, tableName));

        List<Put> list = new LinkedList<>();
        for (Pair<byte[], byte[]> pair : pairList) {
            Put put = new Put(pair.getKey());
            put.addColumn(cf, col, pair.getValue());
            list.add(put);
        }

        table.put(list);
    }



    public void put(String nameSpace, String tableName, byte[] cf, byte[] col, byte[] rowKey, byte[] val) throws IOException {
        Table table = conn.getTable(TableName.valueOf(nameSpace, tableName));
        Put put = new Put(rowKey);
        put.addColumn(cf, col, val);
        table.put(put);
    }


    public List<Pair<byte[], byte[]>> scan(String nameSpace, ScanParameters args) throws IOException {
        List<Pair<byte[], byte[]>> list = new LinkedList<>();
        Table table = conn.getTable(TableName.valueOf(nameSpace, args.getTableName()));

        Scan scan = new Scan().withStartRow(args.getStartRowKey(), false).setLimit(args.getRowCount());
        if (args.getStopRowKey() != null) {
            scan.withStopRow(args.getStopRowKey(), true);
        }
        if (args.getFilter() != null) {
            scan.setFilter(args.getFilter());
        }

        ResultScanner scanner = table.getScanner(scan);
        scanner.forEach(result -> list.add(new Pair<>(result.getRow(), result.getValue(args.getCf(), args.getCol()))));

        return list;
    }

    public void delete(String nameSpace, String tableName, byte[] cf, byte[] col, byte[] rowKey) throws IOException {
        Table table = conn.getTable(TableName.valueOf(nameSpace, tableName));
        Delete del = new Delete(rowKey);
        del.addColumn(cf,col);
        table.delete(del);
    }

    public byte[] get(String nameSpace, String tableName, byte[] cf, byte[] col, byte[] rowKey) throws IOException {
        Table table = conn.getTable(TableName.valueOf(nameSpace, tableName));
        Get get = new Get(rowKey);
        Result result = table.get(get);
        return result.getValue(cf, col);
    }

    public Pair<byte[], byte[]> getKV(String nameSpace, String tableName, byte[] cf, byte[] col, byte[] rowKey) throws IOException {
        Table table = conn.getTable(TableName.valueOf(nameSpace, tableName));

        Get get = new Get(rowKey);
        Result result = table.get(get);
        // result.getValue(cf, col);
        return new Pair<>(result.getRow(), result.getValue(cf, col));
    }

    public boolean checkAndPut(String nameSpace, String tableName, byte[] cf, byte[] col, byte[] rowKey, byte[] expect, byte[] value) throws IOException {
        Table table = conn.getTable(TableName.valueOf(nameSpace, tableName));

        Put put = new Put(rowKey);
        put.addColumn(cf, col, value);

        return table.checkAndPut(rowKey, cf, col, expect, put);
    }

    /**
     * 查询参数对象
     */
    public static class ScanParameters {
        private String tableName;
        private byte[] cf;
        private byte[] col;
        private byte[] startRowKey;
        private byte[] stopRowKey;
        private int rowCount;
        private Filter filter;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public byte[] getCf() {
            return cf;
        }

        public void setCf(byte[] cf) {
            this.cf = cf;
        }

        public byte[] getCol() {
            return col;
        }

        public void setCol(byte[] col) {
            this.col = col;
        }

        public byte[] getStartRowKey() {
            return startRowKey;
        }

        public void setStartRowKey(byte[] startRowKey) {
            this.startRowKey = startRowKey;
        }

        public byte[] getStopRowKey() {
            return stopRowKey;
        }

        public void setStopRowKey(byte[] stopRowKey) {
            this.stopRowKey = stopRowKey;
        }

        public int getRowCount() {
            return rowCount;
        }

        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        public Filter getFilter() {
            return filter;
        }

        public void setFilter(Filter filter) {
            this.filter = filter;
        }
    }



}