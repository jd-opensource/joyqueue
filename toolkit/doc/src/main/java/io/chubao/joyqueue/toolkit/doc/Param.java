package io.chubao.joyqueue.toolkit.doc;

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
