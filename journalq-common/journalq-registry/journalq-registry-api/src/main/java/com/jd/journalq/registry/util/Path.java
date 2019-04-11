/**
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
package com.jd.journalq.registry.util;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 节点路径
 */
public class Path {
    // 路径
    private String path;

    public Path() {
    }

    public Path(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isEmpty() {
        return path == null || path.isEmpty();
    }

    /**
     * 追加子路径
     *
     * @param children 孩子节点
     * @return 全路径
     */
    public Path append(final String... children) {
        StringBuilder builder = new StringBuilder();
        String root = path;
        root = root == null || root.isEmpty() ? "/" : root.trim();
        root = root.isEmpty() ? "/" : root;
        if (root.charAt(0) != '/') {
            builder.append('/');
        }
        builder.append(root);
        if ((children == null) || (children.length == 0)) {
            return new Path(builder.toString());
        }
        int len = root.length();
        if (root.charAt(len - 1) != '/') {
            builder.append('/');
        }
        for (String child : children) {
            if (child == null) {
                continue;
            }
            child = child.trim();
            len = child.length();
            if (len == 0 || (len == 1 && child.charAt(0) == '/')) {
                continue;
            }
            if (child.charAt(0) == '/') {
                builder.append(child, 1, len);
            } else {
                builder.append(child);
            }
            if (child.charAt(len - 1) != '/') {
                builder.append('/');
            }
        }
        if (builder.length() > 1 && builder.charAt(builder.length() - 1) == '/') {
            return new Path(builder.substring(0, builder.length() - 1));
        } else {
            return new Path(builder.toString());
        }
    }

    /**
     * 获取父节点路径
     *
     * @return 父节点路径
     */
    public Path parent() {
        if (path == null) {
            return null;
        }
        int pos = path.lastIndexOf('/');
        if (pos <= 0) {
            return new Path("");
        }
        return new Path(path.substring(0, pos));
    }

    /**
     * 获取叶子节点
     *
     * @return the node
     */
    public Path node() {
        int i = path.lastIndexOf('/');
        if (i < 0) {
            return this;
        }
        if ((i + 1) >= path.length()) {
            return new Path("");
        }
        return new Path(path.substring(i + 1));
    }

    /**
     * 获取所有节点
     *
     * @return 节点列表
     */
    public List<String> nodes() {
        List<String> result = new ArrayList<String>();
        if (path != null && !path.isEmpty()) {
            String token;
            StringTokenizer tokenizer = new StringTokenizer(path, "/");
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
                if (!token.isEmpty()) {
                    result.add(token);
                }
            }
        }
        return result;
    }


    /**
     * 验证路径是否合法
     *
     * @throws IllegalArgumentException if the path is invalid
     */
    public void validate() throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        if (path.length() == 0) {
            throw new IllegalArgumentException("path length must be > 0");
        }
        if (path.charAt(0) != '/') {
            throw new IllegalArgumentException("path must start with / character");
        }
        if (path.length() == 1) { // done checking - it's the root
            return;
        }
        if (path.charAt(path.length() - 1) == '/') {
            throw new IllegalArgumentException("path must not end with / character");
        }

        String reason = null;
        char lastc = '/';
        char chars[] = path.toCharArray();
        char c;
        for (int i = 1; i < chars.length; lastc = chars[i], i++) {
            c = chars[i];

            if (c == 0) {
                reason = "null character not allowed @" + i;
                break;
            } else if (c == '/' && lastc == '/') {
                reason = "empty node name specified @" + i;
                break;
            } else if (c == '.' && lastc == '.') {
                if (chars[i - 2] == '/' && ((i + 1 == chars.length) || chars[i + 1] == '/')) {
                    reason = "relative paths not allowed @" + i;
                    break;
                }
            } else if (c == '.') {
                if (chars[i - 1] == '/' && ((i + 1 == chars.length) || chars[i + 1] == '/')) {
                    reason = "relative paths not allowed @" + i;
                    break;
                }
            } else if (c > '\u0000' && c < '\u001f' || c > '\u007f' && c < '\u009F' || c > '\ud800' && c < '\uf8ff'
                    || c > '\ufff0' && c < '\uffff') {
                reason = "invalid charater @" + i;
                break;
            }
        }

        if (reason != null) {
            throw new IllegalArgumentException("Invalid path string \"" + path + "\" caused by " + reason);
        }
    }

    /**
     * 验证路径
     *
     * @param path 路径
     * @throws IllegalArgumentException
     */
    public static void validate(final String path) throws IllegalArgumentException {
        create(path).validate();
    }

    /**
     * 构造路径
     *
     * @param path
     * @return
     */
    public static Path create(final String path) {
        return new Path(path);
    }

    /**
     * 创建路径
     *
     * @param parent   父节点
     * @param children 孩子节点
     * @return 全路径
     */
    public static String concat(final String parent, final String... children) {
        return create(parent).append(children).getPath();
    }

    /**
     * 获取叶子节点
     *
     * @param path 路径
     * @return 叶子节点
     */
    public static String node(final String path) {
        return create(path).node().getPath();
    }

    /**
     * 获取子节点
     *
     * @param path 路径
     * @return 子节点
     */
    public static List<String> nodes(final String path) {
        return create(path).nodes();
    }

    /**
     * 获取父节点路径
     *
     * @param path 路径
     * @return 父节点路径
     */
    public static String parent(final String path) {
        return create(path).parent().getPath();
    }

}
