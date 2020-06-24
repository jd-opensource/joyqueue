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

package org.joyqueue.handler.jwt;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import org.jose4j.lang.JoseException;
import org.joyqueue.model.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author jiangnan53
 * @date 2020/6/24
 **/
public class JwtUtils {

    private static final Logger logger  = LoggerFactory.getLogger(JwtUtils.class);

    private static final Properties JWT_CONFIG;

    private final String issuer;
    private final double expireInMinute;
    private final String subject;
    private final List<String> audiences = Collections.singletonList("audience");

    static {
        JWT_CONFIG = new Properties();
        try {
            JWT_CONFIG.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
            logger.info("S3 config loaded successfully. ");
        } catch (IOException e) {
            logger.error("Failed to read S3 config , error: {}", e.getMessage());
        }
    }

    private RsaJsonWebKey rsaJsonWebKey;

    public JwtUtils(){
        issuer = JWT_CONFIG.getProperty(JwtConfigKey.JWT_ISSUER.getName(), JwtConfigKey.JWT_ISSUER.getValue().toString());
        expireInMinute = Double.parseDouble(JWT_CONFIG.getProperty(JwtConfigKey.JWT_EXPIRATION_TIME_MINUTES_IN_FUTURE.getName(), JwtConfigKey.JWT_EXPIRATION_TIME_MINUTES_IN_FUTURE.getValue().toString()));
        subject = JWT_CONFIG.getProperty(JwtConfigKey.JWT_SUBJECT.getName(), JwtConfigKey.JWT_SUBJECT.getValue().toString());
        try {
            rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
            rsaJsonWebKey.setKeyId("joyqueue-web");
        }catch (Exception e) {
            logger.error("", e);
        }
    }


    public String createJwt(User user) throws JoseException {

        JwtClaims claims=new JwtClaims();
        claims.setIssuer(issuer);
        claims.setAudience(audiences);
        claims.setExpirationTimeMinutesInTheFuture((float) expireInMinute);
        claims.setGeneratedJwtId();
        claims.setStringClaim("username",String.valueOf(user.getCode()));
        claims.setStringClaim("password",user.getPassword());
        /*
         * token发布的时间
         */
        claims.setIssuedAtToNow();
        claims.setSubject(subject);

        JsonWebSignature jws=new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        return jws.getCompactSerialization();
    }


    public JwtClaims decodeJwt(String token) throws MalformedClaimException {
        JwtConsumer jwtConsumer=new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setRequireSubject()
                .setRequireIssuedAt()
                .setRequireJwtId()
                .setRequireNotBefore()
                .setExpectedAudience(audiences.toArray(new String[0]))
                .setExpectedSubject(subject)
                .setExpectedIssuer(issuer)
                .setVerificationKey(rsaJsonWebKey.getKey())
                .setSkipSignatureVerification()
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                        AlgorithmIdentifiers.RSA_USING_SHA256))
                .build();
        try {
            return jwtConsumer.processToClaims(token);
        }catch (InvalidJwtException e){
            if(e.hasExpired()){
                logger.error("jwt失效时间:"+e.getJwtContext().getJwtClaims().getExpirationTime());
            }

            if(e.hasErrorCode(ErrorCodes.AUDIENCE_INVALID)){
                logger.error("jwt的audience无效,audience:"+e.getJwtContext().getJwtClaims().getAudience());
            }
            return null;
        }
    }
}
