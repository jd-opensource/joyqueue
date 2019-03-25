package com.jd.journalq.handler.util;

import com.jd.journalq.model.Pager;
import com.jd.journalq.model.Pagination;

public class PageUtil {

    public  static Pagination pagination(Pager pager){
        Pagination pagination= new Pagination();
        pagination.setPage(pager.getPage());
        pagination.setSize(pager.getPageSize());
        pagination.setTotalRecord(pager.getTotal());
        return  pagination;
    }
}
