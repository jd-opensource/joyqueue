package io.chubao.joyqueue.model;

public class PageWithExtra<T> {
    private PageResult<T> pageResult;
    private Object extras;

    public PageResult<T> getPageResult() {
        return pageResult;
    }

    public void setPageResult(PageResult<T> pageResult) {
        this.pageResult = pageResult;
    }

    public Object getExtras() {
        return extras;
    }

    public void setExtras(Object extras) {
        this.extras = extras;
    }
}
