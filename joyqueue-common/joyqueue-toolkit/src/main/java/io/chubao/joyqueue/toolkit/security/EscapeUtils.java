package io.chubao.joyqueue.toolkit.security;

/**
 * @author wylixiaobin
 * Date: 2018/11/27
 */
public class EscapeUtils {
    /**
     *
     * @param str 要替换的字符串
     * @param signS 被替换的符号
     * @param signT 要替换成的符号
     * @return
     */
    public static String escape(String str,String signS,String signT){
        return str.replace(signS,signT);
    }

    public static String escapeTopic(String topic){
        return escape(topic,"/","@");
    }
    public static String reEscapeTopic(String topic){
        return escape(topic,"@","/");
    }
}
