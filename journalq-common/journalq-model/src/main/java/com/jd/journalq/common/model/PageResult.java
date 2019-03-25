package com.jd.journalq.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询结果
 * Created by yangyang115 on 18-7-26.
 */
public class PageResult<M> {
    //分页条件
    private Pagination pagination;
    //结果
    private List<M> result;

    public PageResult() {
    }

    public PageResult(Pagination pagination, List<M> result) {
        this.pagination = pagination;
        this.result = result;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<M> getResult() {
        return result;
    }

    public void setResult(List<M> result) {
        this.result = result;
    }

    public static <M> PageResult<M> empty() {
        return new PageResult<>(new Pagination(0), new ArrayList<>(0));
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "pagination=" + pagination +
                ", result=" + result +
                '}';
    }
}
