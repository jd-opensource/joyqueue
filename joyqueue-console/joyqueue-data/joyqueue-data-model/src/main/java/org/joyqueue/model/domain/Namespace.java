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
package org.joyqueue.model.domain;

import org.joyqueue.model.domain.nsr.BaseNsrModel;

/**
 * 命名空间
 * Created by chenyanying3 on 2018-11-17
 */
public class Namespace extends BaseNsrModel {
//    public static final Identity DEFAULT_NAMESPACE_IDENTITY = new Identity(0l,"");
    public static final String DEFAULT_NAMESPACE_ID = "";
    public static final String DEFAULT_NAMESPACE_CODE = "";

    private String code = DEFAULT_NAMESPACE_CODE;
    private String name;

    public Namespace() {
    }

    public Namespace(String code) {
        this.code = code;
        this.id = code;
    }

    public Namespace(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Namespace{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
