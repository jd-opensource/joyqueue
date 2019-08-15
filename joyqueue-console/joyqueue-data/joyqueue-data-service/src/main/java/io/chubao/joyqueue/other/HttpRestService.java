package io.chubao.joyqueue.other;

import io.chubao.joyqueue.monitor.RestResponse;

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
