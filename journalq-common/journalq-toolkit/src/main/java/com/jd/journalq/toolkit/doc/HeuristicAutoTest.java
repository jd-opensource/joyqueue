package com.jd.journalq.toolkit.doc;

import java.util.List;

public interface HeuristicAutoTest<T> {
    /**
     *  test t
     **/
    TestCase test(List<Class> paramClasses, T t) throws Exception;
}
