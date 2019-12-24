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

/**
 * Created by yangyang115 on 18-7-26.
 */
public class Pagination {

    //总记录数
    private int totalRecord;

    public static final int SIZE = 10;
    public static final int MAX_SIZE = Integer.MAX_VALUE;
    //页大小
    private int size = SIZE;
    //起始记录条数
    private int start = 0;
    private int page = 1;
    //总的页数
    private int pages;

    public Pagination() {
    }

    public Pagination(final int start) {
        this(start, SIZE);
    }

    public Pagination(final int start, final int size) {
        this.start = start < 0 ? 0 : start;
        this.size = size < 1 || size > MAX_SIZE ? SIZE : size;
        this.page = this.start / this.size + 1;
    }

    public static Pagination newPagination(final Integer start) {
        return new Pagination(start == null ? 0 : start, SIZE);
    }

    public static Pagination newPagination(final Integer start, final Integer size) {
        return new Pagination(start == null ? 0 : start, size == null ? SIZE : size);
    }

    public static Pagination newPagination(final Integer start, final Integer page, final Integer size) {
        if (page != null) {
            int v = size < 1 || size > MAX_SIZE ? SIZE : size;
            int p = page <= 0 ? 1 : page;
            return newPagination((p - 1) * v, v);
        } else {
            return newPagination(start, size);
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStart() {
        if (page > 1) {
            this.start = (page-1)*size;
        }
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        if (totalRecord > 0) {
            this.totalRecord = totalRecord;
            int pages = totalRecord / size;
            if (totalRecord % size > 0) {
                pages++;
            }
            this.pages = pages;
            this.page = page > pages ? pages : page;
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPages() {
        return pages;
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "size=" + size +
                ", start=" + start +
                ", page=" + page +
                ", pages=" + pages +
                ", totalRecord=" + totalRecord +
                '}';
    }
}
