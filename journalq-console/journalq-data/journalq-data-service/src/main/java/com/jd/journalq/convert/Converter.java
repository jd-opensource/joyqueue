package com.jd.journalq.convert;

/**
 * 模型对象转换器
 *
 * @param <S> 原始对象
 * @param <T> 目标对象
 */
public abstract class Converter<S, T> {

    private boolean checkNull = true;

    Converter() {
    }

    Converter(final boolean checkNull) {
        this.checkNull = checkNull;
    }

    /**
     * Forward converter, S->T
     * @param s
     * @return
     */
    protected abstract T forward(final S s);

    /**
     * Backward converter, T->S
     * @param t
     * @return
     */
    protected abstract S backward(final T t);

    /**
     * Convert S->T
     *
     * @param s 原始对象
     * @return 目标对象
     */
    public T convert(S s) {
        if (checkNull) {
            return null == s ? null : forward(s);
        } else {
            return forward(s);
        }
    }

    /**
     * Revert T->S
     *
     * @param t 目标对象
     * @return 原始对象
     */
    public S revert(T t){
        if (checkNull) {
            return null == t ? null : backward(t);
        } else {
            return backward(t);
        }
    }

}
