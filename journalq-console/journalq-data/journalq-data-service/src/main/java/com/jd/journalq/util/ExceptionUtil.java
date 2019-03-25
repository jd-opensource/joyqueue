package com.jd.journalq.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionUtil {
    public static String getStackTrace(Exception e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        e.printStackTrace(pout);
        String result = new String(out.toByteArray());
        pout.close();
        try {
            out.close();
        } catch (Exception e1) {
            return "";
        }
        return result;
    }
}
