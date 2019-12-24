/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.model;

import java.io.Serializable;
import java.util.List;

/**
 * 分页
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class Pager<T> implements Serializable {

    private int page;
    private int pageSize;
    private int total;
    private int pages;
    private List<T> data;

    public Pager() {
    }

    public Pager(int page, int pageSize, int total, List<T> data) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.data = data;
        if (total != 0) {
            this.pages = total / pageSize;
            if (total % pageSize != 0) {
                this.pages++;
            }
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getPages() {
        return pages;
    }
}