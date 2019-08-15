package io.chubao.joyqueue.test;

import java.nio.charset.Charset;

/**
 * Created by wangxiaofei1 on 2018/10/19.
 */
public class ConfigITTest {
    public static void main(String[] args) {
        System.out.println(str2HexStr("erp:wangxiaofei7"));
        System.out.println(hexStr2Str("0861E51E55B2C7D968E89BCDC2436BEB"));
    }
    /**
       * 字符串转换成为16进制(无需Unicode编码)
       * @param str
       * @return
       */
    public static String str2HexStr(String str) { 
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes(Charset.forName("UTF-8"));
        int bit;
        for (int i = 0; i < bs.length; i++) { 
            bit = (bs[i] & 0x0f0) >> 4;
              sb.append(chars[bit]);
              bit = bs[i] & 0x0f;
              sb.append(chars[bit]);
         // sb.append(' ');
        }
        return sb.toString().trim();
    }
    private static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }
}
