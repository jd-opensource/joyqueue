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
package org.joyqueue.domain;

import java.util.Objects;

/**
 * 命名空间
 */
public class Namespace {
    /**
     * 命名空间代码
     */
    protected String code;
    /**
     * 命名空间名称
     */
    protected String name;


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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Namespace)) return false;
        Namespace namespace = (Namespace) o;
        return Objects.equals(code, namespace.code) &&
                Objects.equals(name, namespace.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code, name);
    }
}
