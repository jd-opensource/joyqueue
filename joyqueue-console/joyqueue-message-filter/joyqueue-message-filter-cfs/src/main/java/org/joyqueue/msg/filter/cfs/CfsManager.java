/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.msg.filter.cfs;

import org.joyqueue.datasource.DataSourceConfig;
import org.joyqueue.datasource.DataSourceFactory;
import org.joyqueue.msg.filter.cfs.model.CfsFileInfo;
import org.joyqueue.toolkit.db.DaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class CfsManager {

    private static final Logger logger = LoggerFactory.getLogger(CfsManager.class);

    private static final Properties CFS_CONFIG;

    private DataSource dataSource;

    private static final String UPDATE_URL_SQL = "UPDATE topic_msg_filter SET url = %s WHERE id = %s";

    private final Region clientRegion;
    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;

    static {
        CFS_CONFIG = new Properties();
        String configPath = CfsManager.class.getResource("/").getPath() + "cfs.properties";
        try {
            InputStream inputStream = new FileInputStream(configPath);
            CFS_CONFIG.load(inputStream);
        } catch (IOException e) {
            logger.error("Failed to read cfs config in path: {}, error: {}", configPath, e.getMessage());
        }
    }

    public CfsManager() {
        clientRegion = Region.of(CFS_CONFIG.getProperty(CfsConfigKey.CFS_REGION.getName(), CfsConfigKey.CFS_REGION.getValue().toString()));
        endpoint = CFS_CONFIG.getProperty(CfsConfigKey.CFS_ENDPOINT.getName(), CfsConfigKey.CFS_REGION.getValue().toString());
        accessKey = CFS_CONFIG.getProperty(CfsConfigKey.CFS_ACCESS_KEY.getName(), CfsConfigKey.CFS_ACCESS_KEY.getValue().toString());
        secretKey = CFS_CONFIG.getProperty(CfsConfigKey.CFS_SECRET_KEY.getName(), CfsConfigKey.CFS_SECRET_KEY.getValue().toString());
        bucketName = CFS_CONFIG.getProperty(CfsConfigKey.CFS_BUCKET_NAME.getName(), CfsConfigKey.CFS_BUCKET_NAME.getValue().toString());
    }

    public void upload(long id, long userId, String path) {
        String objectKey = '/' + userId + '/' + path;
        try {
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            S3Configuration configuration = S3Configuration.builder()
                    .chunkedEncodingEnabled(true)
                    .build();
            S3Client client = S3Client.builder()
                    .region(clientRegion)
                    .credentialsProvider(credentialsProvider)
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(configuration)
                    .build();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            RequestBody requestBody = RequestBody.fromFile(Paths.get(path));
            PutObjectResponse putObjectResponse = client.putObject(putObjectRequest, requestBody);
            logger.info("cfs: upload file success");
            String url = getCfsUrl(clientRegion, endpoint, objectKey, bucketName, configuration);
            updateUrl(new CfsFileInfo(id, url));
            logger.info("userId: {}, url:{}", userId, url);
        } catch (Exception e) {
            logger.error("cfs: failed to upload file :{}, error: {}", path, e.getMessage());
        }
    }

    private String getCfsUrl(Region region, String endpoint, String objectKey, String bucketName, S3Configuration configuration) throws URISyntaxException {
        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucketName).endpoint(new URI(endpoint)).key(objectKey).build();
        S3Utilities s3Utilities = S3Utilities.builder().region(region).s3Configuration(configuration).build();
        URL url = s3Utilities.getUrl(getUrlRequest);
        return url.toString();
    }

    private void updateUrl(CfsFileInfo cfsFileInfo) throws Exception {
        DaoUtil.update(getDataSource(), cfsFileInfo, UPDATE_URL_SQL, (statement, target) -> {
            statement.setLong(1, target.getId());
            statement.setString(2, target.getUrl());
        });
    }

    public void delete(String url) {
        String bucketName = "";
        String objectKey = "";

        try {
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            S3Configuration configuration = S3Configuration.builder()
                    .chunkedEncodingEnabled(true)
                    .build();
            S3Client client = S3Client.builder()
                    .region(clientRegion)
                    .credentialsProvider(credentialsProvider)
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(configuration)
                    .build();
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            client.deleteObject(deleteObjectRequest);
            logger.info("cfs: delete file success");
        } catch (Exception e) {
            logger.error("cfs: failed to delete file :{}", url);
        }
    }

    private DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = DataSourceFactory.build(initDataSourceConfig());
        }
        return dataSource;
    }

    private DataSourceConfig initDataSourceConfig() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDriver(CFS_CONFIG.getProperty(CfsConfigKey.CFS_JDBC_DRIVER.getName(), CfsConfigKey.CFS_JDBC_DRIVER.getValue().toString()));
        dataSourceConfig.setUrl(CFS_CONFIG.getProperty(CfsConfigKey.CFS_JDBC_URL.getName(), CfsConfigKey.CFS_JDBC_URL.getValue().toString()));
        dataSourceConfig.setUser(CFS_CONFIG.getProperty(CfsConfigKey.CFS_JDBC_USERNAME.getName(), CfsConfigKey.CFS_JDBC_USERNAME.getValue().toString()));
        dataSourceConfig.setPassword(CFS_CONFIG.getProperty(CfsConfigKey.CFS_JDBC_PASSWORD.getName(), CfsConfigKey.CFS_JDBC_PASSWORD.getValue().toString()));
        return dataSourceConfig;
    }
}
