package com.jd.journalq.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.journalq.monitor.RestResponse;
import com.jd.journalq.monitor.RestResponseCode;
import com.jd.journalq.exception.ServiceException;

import java.io.IOException;
import java.util.List;

/**
 * @author wangjin
 * @time  2018-12-03
 * @mail  wangjin18@jd.com
 *
 **/
public class JSONParser {
    public static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T parse(String content,Class restClass,Class dataClass) throws IOException {
        JavaType javaType =composite(restClass, dataClass);
        return mapper.readValue(content, javaType);
    }

    public static <T> T parseList(String content,Class restClass,Class dataClass) throws IOException {
        JavaType javaType =compositeList(restClass, dataClass);
        return mapper.readValue(content, javaType);
    }


    /**
     *  composite class a and b to a Java Type
     *
     **/
    public  static JavaType composite(Class a,Class b ){
        return mapper.getTypeFactory().constructParametricType(a, b);
    }


    /**
     *
     *
     **/
    private static JavaType compositeList(Class a,Class listClass ){
        JavaType listType = mapper.getTypeFactory().constructParametricType(List.class,listClass);
        return mapper.getTypeFactory().constructParametricType(a,listType);
    }



    /**
     * @param list true indicate body is a list<dataClass> or
     * @return  response
     * @throws ServiceException when response not success
     **/
    public static <T> RestResponse<T> parse(String content, Class restResponse, Class dataClass, boolean list){
        RestResponse<T> response=null;
        try {
            if(list){
                response=JSONParser.parseList(content, restResponse, dataClass);
            }else {
                response = JSONParser.parse(content, restResponse, dataClass);
            }
            if(response.getCode()!= RestResponseCode.SUCCESS.getCode()){
                throw  new ServiceException(response.getCode(),response.getMessage()) ;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return  response;
    }







}
