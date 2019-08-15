package io.chubao.joyqueue.handler.util;

import io.chubao.joyqueue.model.Pager;
import io.chubao.joyqueue.model.Pagination;

public class PageUtil {

    public  static Pagination pagination(Pager pager){
        Pagination pagination= new Pagination();
        pagination.setPage(pager.getPage());
        pagination.setSize(pager.getPageSize());
        pagination.setTotalRecord(pager.getTotal());
        return  pagination;
    }
}
