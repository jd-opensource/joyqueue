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
package org.joyqueue.async;


import org.joyqueue.model.domain.Subscribe;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 *   支持对topic 所在broker 的并行查询
 *
 **/
public interface BrokerClusterQuery<C> {

    /**
     *用于DELETE方法
     * @param brokerIp   关闭的brokerIp
     * @param condition  查询条件
     * @param provider   provider path and result key
     * @param pathKey    获取路径的key, 用于从 urlMappingService 查询 path template
     * @param logKey     for log trace
     * @return  a map with String,String, broker 维度
     */
    Future<Map<String, String>> asyncDeleteOnBroker(Integer brokerId,Subscribe condition, RetrieveProvider<Subscribe> provider, String pathKey, String logKey);
    /**
     * 查询所有信息
     * @param namespace
     * @param topic
     * @param groupNo
     * @param path
     * @param logkey
     * @return
     */
    Future<Map<String, String>> asyncQueryAllBroker(String namespace,String topic,Integer groupNo, String path,String logkey);
    /**
     * @param condition  查询条件
     * @param provider   provider path and result key
     * @param pathKey    获取路径的key, 用于从 urlMappingService 查询 path template
     * @param logKey     for log trace
     * @return  a map with String,String, broker 维度
     *
     *
     **/
    Future<Map<String,String>> asyncQueryOnBroker(C condition, RetrieveProvider<C> provider, String pathKey, String logKey);


    /**
     * 用于PUT、POST 方法的请求
     * @param condition  查询条件
     * @param provider   provider path and result key
     * @param pathKey    获取路径的key, 用于从 urlMappingService 查询 path template
     * @param logKey     for log trace
     * @return  a map with String,String, broker 维度
     *
     *
     **/
    Future<Map<String,String>> asyncUpdateOnBroker(C condition, UpdateProvider<C> provider, String pathKey, String logKey);




    /**
     * 用于PUT、POST 方法的请求
     * @param condition  查询条件
     * @param provider   provider path and result key
     * @param pathKey    获取路径的key, 用于从 urlMappingService 查询 path template
     * @param logKey     for log trace
     * @return  a map with String,String, broker 维度
     *
     *
     **/
    Future<Map<String,String>> asyncUpdateOnPartitionGroup(C condition, UpdateProvider<C> provider, String pathKey, String logKey);



    /**
     * 用于PUT、POST 方法的请求
     * @param condition  查询条件
     * @param provider   provider path and result key
     * @param pathKey    获取路径的key, 用于从 urlMappingService 查询 path template
     * @param logKey     for log trace
     * @return  a map with String,String, broker 维度
     *
     *
     **/
    Future<Map<String,String>> asyncUpdateOnPartition(C condition, UpdateProvider<C> provider, String pathKey, String logKey);

    /**
     * @param condition  查询条件
     * @param provider   provider path and result key
     * @param pathKey    获取路径的key, 用于从 urlMappingService 查询 path template
     * @param logKey     for log trace
     * @return  a map with String,String,partition group 维度
     *
     **/
    Future<Map<String,String>> asyncQueryOnPartitionGroup(C condition, RetrieveProvider<C> provider, String pathKey, String logKey);


    /**
     * 阻塞 ,等待请求返回或者 超时
     **/
    Map<String,String> get(Future<Map<String,String>> resultFuture, long timeout, TimeUnit unit);

}
