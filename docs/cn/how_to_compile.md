# 编译

编译JoyQueue需要的环境包括：

* JDK 8 及以上的版本
* Maven 3 及以上的版本

## 下载源代码

使用git下载源代码：

```bash
git clone https://github.com/joyqueue/joyqueue.git
```

或者[下载源代码压缩包](https://github.com/joyqueue/joyqueue/archive/master.zip)，然后解压到目录joyqueue。

## 编译JoyQueue

编译JoyQueue需要的环境包括：

* JDK 8 及以上的版本
* Maven 3 及以上的版本

执行编译：

```bash
mvn -PCompileFrontend -Dmaven.test.skip install
```

编译过程中会自动下载Node.js和npm到源代码目录，并使用node编译JoyQueue Web的前端页面，如果不需要编译JoyQueue Web可以执行：

```bash
mvn -Dmaven.test.skip install
```

编译完成后，生成的安装包位于：

* **JoyQueue Server**: joyqueue/joyqueue-distribution/joyqueue-distribution-server/target
* **JoyQueue Web**: joyqueue/joyqueue-distribution/joyqueue-distribution-web/target
* **JoyQueue Client**: joyqueue/joyqueue-client/joyqueue-client-all-shaded/target/joyqueue-client-all-shaded-4.1.1.jar
