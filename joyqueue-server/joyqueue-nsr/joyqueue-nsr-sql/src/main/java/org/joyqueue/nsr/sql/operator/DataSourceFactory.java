package org.joyqueue.nsr.sql.operator;

import com.jd.laf.extension.Type;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * DataSourceFactory
 * author: gaohaoxiang
 * date: 2019/8/1
 */
public interface DataSourceFactory extends Type<String> {

    DataSource createDataSource(Properties properties);
}