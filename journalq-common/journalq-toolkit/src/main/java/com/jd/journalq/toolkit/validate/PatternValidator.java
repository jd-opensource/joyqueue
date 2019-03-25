package com.jd.journalq.toolkit.validate;


import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证不能为空，支持字符序列，集合，散列，数组
 * Created by hexiaofeng on 16-5-10.
 */
public class PatternValidator implements Validator {

    public static final PatternValidator INSTANCE = new PatternValidator();
    // 缓存编译的模板
    protected ConcurrentMap<String, Pattern> patterns = new ConcurrentHashMap<String, Pattern>();

    @Override
    public void validate(final Object target, final Annotation annotation, final Value value) throws ValidateException {
        com.jd.journalq.toolkit.validate.annotation.Pattern pattern =
                (com.jd.journalq.toolkit.validate.annotation.Pattern) annotation;
        String v = value.value == null ? "" : value.value.toString();
        Pattern p = patterns.get(v);
        if (p == null) {
            p = Pattern.compile(v);
            patterns.putIfAbsent(v, p);
        }
        Matcher matcher = p.matcher(v);
        if (matcher.find() == pattern.flag()) {
            if (pattern.message() == null || pattern.message().isEmpty()) {
                throw new ValidateException(String.format("%s is not match with %s.", value.name, v));
            }
            throw new ValidateException(String.format(pattern.message(), value.name, v));
        }
    }
}
