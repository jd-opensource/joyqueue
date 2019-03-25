package com.jd.journalq.util;

import com.jd.journalq.toolkit.URL;

public class UrlEncoderUtil {

    /**
     *
     * 对参数 encodeParam
     *
     **/
    public static String[] encodeParam(String... args){
        String[] encoded = new String[args.length];
        for (int i = 0;i<args.length;i++){
            encoded[i] = URL.encode(args[i]);
        }
        return encoded;
    }
}
