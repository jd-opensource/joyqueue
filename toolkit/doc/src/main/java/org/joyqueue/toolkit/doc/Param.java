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
package org.joyqueue.toolkit.doc;

public class Param {
    private String name;
    private String type;
    private String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        int hash=0;
        if(name!=null){
           hash=hash*31+name.hashCode();
        }
        if(type!=null){
           hash=hash*31+type.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this) {
            return true;
        }
        if(obj instanceof Param){
           Param p= (Param)obj;
           if(!p.name.equals(this.name)){ return false;}
           if(!p.type.equals(this.type)){ return false;}
           if(p.name==null||this.name==null) {return false;}
           if(p.type==null||this.type==null) {return false;}
        }
        return  true;
    }
}
