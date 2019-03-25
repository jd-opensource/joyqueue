package com.jd.journalq.other;

import com.jd.journalq.common.monitor.RestResponse;

//todo 添加一个broker service 抽象类，把这些东西放到抽象类里面去
public interface HttpRestService {

      /**
       * @param pathKey  key in monitorUrl mapping
       * @param dataClass  data model
       * @param isList    data is list Object
       * @param args  request params
       **/
     <T> RestResponse<T> get(String pathKey,Class dataClass,boolean isList,String... args);

    /**
     * @param pathKey  key in monitorUrl mapping
     * @param dataClass  data model
     * @param isList    data is list Object
     * @param args  request params
     **/
    <T> RestResponse<T> put(String pathKey,Class dataClass,boolean isList,String content,String... args);
}
