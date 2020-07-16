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
package org.joyqueue.msg.filter.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class S3Manager {

    private static final Logger logger = LoggerFactory.getLogger(S3Manager.class);

    private static final Properties S3_CONFIG;

    private final Region clientRegion;
    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;

    static {
        S3_CONFIG = new Properties();
        try {
            S3_CONFIG.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
            logger.info("S3 config loaded successfully. ");
        } catch (IOException e) {
            logger.error("Failed to read S3 config , error: {}", e.getMessage());
        }
    }

    public S3Manager() {
        clientRegion = Region.of(S3_CONFIG.getProperty(S3ConfigKey.S3_REGION.getName(), S3ConfigKey.S3_REGION.getValue().toString()));
        endpoint = S3_CONFIG.getProperty(S3ConfigKey.S3_ENDPOINT.getName(), S3ConfigKey.S3_REGION.getValue().toString());
        accessKey = S3_CONFIG.getProperty(S3ConfigKey.S3_ACCESS_KEY.getName(), S3ConfigKey.S3_ACCESS_KEY.getValue().toString());
        secretKey = S3_CONFIG.getProperty(S3ConfigKey.S3_SECRET_KEY.getName(), S3ConfigKey.S3_SECRET_KEY.getValue().toString());
        bucketName = S3_CONFIG.getProperty(S3ConfigKey.S3_BUCKET_NAME.getName(), S3ConfigKey.S3_BUCKET_NAME.getValue().toString());
        logger.info(String.format("S3 config, client region: %s, endpoint: %s, access key: %s, secret key: %s, bucket name: %s ",
                clientRegion, endpoint, accessKey, secretKey, bucketName));
    }

    public String upload(String path) {
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
                    .contentType("application/octet-stream")
                    .bucket(bucketName)
                    .key(path)
                    .build();
            RequestBody requestBody = RequestBody.fromFile(Paths.get(path));
            client.putObject(putObjectRequest, requestBody);
            logger.info("S3: upload file success");
            String url = getS3Url(path);
            logger.info("url:{}", url);
            return url;
        } catch (Exception e) {
            logger.error("S3: failed to upload file :{}, error: {}", path, e.getMessage());
        }
        return null;
    }

    private String getS3Url(String objectKey) {
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
}
