package com.jd.journalq.service.impl;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public interface HttpService {

    CloseableHttpResponse executeRequest(HttpUriRequest request) throws Exception;
}
