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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.Properties;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class CfsManager {

    private static final Logger logger = LoggerFactory.getLogger(CfsManager.class);

    private static final Properties CFS_CONFIG;

    private DataSource dataSource;

    private static final String UPDATE_URL_SQL = "UPDATE topic_msg_filter SET url = ? , obj_key = ? WHERE id = ?";
    private static final String FIND_URL_BY_ID_SQL = "SELECT url FROM topic_msg_filter WHERE id = ?";

    private final Region clientRegion;
    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;

    static {
        CFS_CONFIG = new Properties();
        String configPath = CfsManager.class.getResource("/").getPath() + "application.properties";
        try {
            InputStream inputStream = new FileInputStream(configPath);
            CFS_CONFIG.load(inputStream);
        } catch (IOException e) {
            logger.error("Failed to read cfs config in path: {}, error: {}", configPath, e.getMessage());
        }
    }

    public CfsManager() {
        dataSource = getDataSource();
        clientRegion = Region.of(CFS_CONFIG.getProperty(CfsConfigKey.CFS_REGION.getName(), CfsConfigKey.CFS_REGION.getValue().toString()));
        endpoint = CFS_CONFIG.getProperty(CfsConfigKey.CFS_ENDPOINT.getName(), CfsConfigKey.CFS_REGION.getValue().toString());
        accessKey = CFS_CONFIG.getProperty(CfsConfigKey.CFS_ACCESS_KEY.getName(), CfsConfigKey.CFS_ACCESS_KEY.getValue().toString());
        secretKey = CFS_CONFIG.getProperty(CfsConfigKey.CFS_SECRET_KEY.getName(), CfsConfigKey.CFS_SECRET_KEY.getValue().toString());
        bucketName = CFS_CONFIG.getProperty(CfsConfigKey.CFS_BUCKET_NAME.getName(), CfsConfigKey.CFS_BUCKET_NAME.getValue().toString());
    }

    public void upload(long id, long userId, String path) {
        try {
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            S3Configuration configuration = S3Configuration.builder()
                    .chunkedEncodingEnabled(true)
                    .pathStyleAccessEnabled(true)
                    .build();
            S3Client client = S3Client.builder()
                    .region(clientRegion)
                    .credentialsProvider(credentialsProvider)
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(configuration)
                    .build();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();
            RequestBody requestBody = RequestBody.fromFile(Paths.get(path));
            client.putObject(putObjectRequest, requestBody);
            logger.info("cfs: upload file success");
            String url = getCfsUrl(path);
            logger.info("userId: {}, url:{}", userId, url);
            updateUrl(new CfsFileInfo(id,path,url));
        } catch (Exception e) {
            logger.error("cfs: failed to upload file :{}, error: {}", path, e.getMessage());
        }
    }

    public InputStream download(long fileId) {
        String url = "<EMPTY>";
        try {
            url = getUrlById(fileId);
            String objectKey = url.substring(url.lastIndexOf('/') + 1);
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            S3Configuration configuration = S3Configuration.builder()
                    .chunkedEncodingEnabled(true)
                    .pathStyleAccessEnabled(true)
                    .build();
            S3Client client = S3Client.builder()
                    .region(clientRegion)
                    .credentialsProvider(credentialsProvider)
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(configuration)
                    .build();
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            InputStream inputStream = client.getObject(request);
            while (true) {
                if (inputStream.read() == -1) {
                    break;
                }
            }
            return inputStream;
        } catch (IOException e) {
            logger.error("Failed to download file which url is {}", url);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
        }
        return null;
    }

    private String getCfsUrl(String objectKey) {
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        S3Presigner preSigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(endpoint))
                .region(clientRegion).build();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest).signatureDuration(Duration.ofDays(7)).build();
        PresignedGetObjectRequest presignedGetObjectRequest = preSigner.presignGetObject(getObjectPresignRequest);
        String url = presignedGetObjectRequest.url().toString();
        preSigner.close();
        return url;
    }

    private String getUrlById(long id) throws Exception {
        return DaoUtil.queryObject(getDataSource(), FIND_URL_BY_ID_SQL, new DaoUtil.QueryCallback<String>() {
            @Override
            public void before(PreparedStatement statement) throws Exception {
                statement.setLong(1, id);
            }

            @Override
            public String map(ResultSet rs) throws Exception {
                return rs.getString("url");
            }
        });
    }

    private void updateUrl(CfsFileInfo cfsFileInfo) throws Exception {
        Preconditions.checkArgument(cfsFileInfo != null);
        int update = DaoUtil.update(getDataSource(), cfsFileInfo, UPDATE_URL_SQL, (statement, target) -> {
            statement.setString(1, target.getUrl());
            statement.setString(2, target.getObjectKey());
            statement.setLong(3, target.getId());
        });
        if (update >= 1) {
            logger.info("update url success,file: {}, url: {}", cfsFileInfo.getId(), cfsFileInfo.getUrl());
        } else {
            logger.warn("Failed to update url [{}] which file id is {}", cfsFileInfo.getUrl(), cfsFileInfo.getId());
        }
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
        String user = CFS_CONFIG.getProperty(CfsConfigKey.CFS_JDBC_USERNAME.getName(), CfsConfigKey.CFS_JDBC_USERNAME.getValue().toString());
        if (StringUtils.isNoneBlank(user)) {
            dataSourceConfig.setUser(user);
        }
        String password = CFS_CONFIG.getProperty(CfsConfigKey.CFS_JDBC_PASSWORD.getName(), CfsConfigKey.CFS_JDBC_PASSWORD.getValue().toString());
        if (StringUtils.isNoneBlank(password)) {
            dataSourceConfig.setPassword(password);
        }
        return dataSourceConfig;
    }
}
