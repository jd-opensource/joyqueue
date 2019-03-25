package com.jd.journalq.common.model;

/**
 * 分页查询条件
 * Created by yangyang115 on 18-7-26.
 */
public class QPageQuery<Q extends Query> extends ListQuery<Q> {

    private Pagination pagination;


    public QPageQuery() {
    }

    public QPageQuery(Pagination pagination, Q query) {
        super(query);
        this.pagination = pagination;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }


    @Override
    public String toString() {
        return "QPageQuery{" +
                "pagination=" + pagination +
                ", query=" + query +
                '}';
    }
}
