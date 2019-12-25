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
