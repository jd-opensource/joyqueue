package io.chubao.joyqueue.toolkit.doc;

import java.util.List;

public interface HeuristicAutoTest<T> {
    /**
     * test t
     **/
    TestCase test(List<Class> paramClasses, T t) throws Exception;
}
