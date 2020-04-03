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
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.net.URI;
import java.nio.file.Paths;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class CfsManager {

    private static final Logger logger = LoggerFactory.getLogger(CfsManager.class);

    public void upload(String path) {
        Region clientRegion = Region.of("");
        String endpoint = "";
        String accessKey = "";
        String secretKey = "";
        String bucketName = "";
        String objectKey = "";

        try {
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey,secretKey));
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
        }catch (Exception e) {
            logger.error("cfs: failed to upload file :{}",path);
        }
    }


    public void delete(String url) {
        Region clientRegion = Region.of("");
        String endpoint = "";
        String accessKey = "";
        String secretKey = "";
        String bucketName = "";
        String objectKey = "";

        try {
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey,secretKey));
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
            DeleteObjectResponse deleteObjectResponse = client.deleteObject(deleteObjectRequest);
            logger.info("cfs: delete file success");
        }catch (Exception e) {
            logger.error("cfs: failed to delete file :{}",url);
        }
    }
}
