package io.chubao.joyqueue.toolkit.io;

import java.util.List;

public class Directory {
    private String name;
    private String path;
    private List<Directory> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Directory> getChildren() {
        return children;
    }

    public void setChildren(List<Directory> children) {
        this.children = children;
    }
}
