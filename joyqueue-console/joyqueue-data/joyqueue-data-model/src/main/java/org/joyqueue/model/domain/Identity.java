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

import javax.validation.constraints.Pattern;

/**
 * Created by yangyang115 on 18-7-26.
 */
public class Identity {

    private Long id;

    @Pattern(regexp = "^[a-zA-Z0-9]+[a-zA-Z0-9_,]*[a-zA-Z0-9]+$", message = "Please enter correct code")
    private String code;

    private String name;


    public Identity() {

    }

    public Identity(Long id) {
        this.id = id;
    }

    public Identity(String code) {
        this.code = code;
    }

    public Identity(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Identity(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Identity(Identifier identifier) {
        if (identifier != null) {
            this.id = identifier.getId();
            this.code = identifier.getCode();
            this.name = identifier.getName();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "Identity{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof Identity){
            Identity i=(Identity)o;
            if(i.id==id){
               if(i.code!=null&&i.code.equals(code))
                   return true;
               else if(code==null)
                   return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return  (int) (id ^ (id >>> 32));
    }
}
