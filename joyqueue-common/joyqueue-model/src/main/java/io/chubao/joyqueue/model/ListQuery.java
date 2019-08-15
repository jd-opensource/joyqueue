package io.chubao.joyqueue.model;

/**
 * @author wylixiaobin
 * Date: 2018/11/1
 */
public class ListQuery<Q> implements Query {
    protected Q query;

    public ListQuery(){
    }
    public ListQuery(Q query){
        this.query = query;
    }
    public Q getQuery() {
        return query;
    }

    public void setQuery(Q query) {
        this.query = query;
    }
}
