# 版本

**master** 上线分支  
**4.0.x** 当前稳定版本，只修改bug，不接受新功能。  
**4.1.0-SNAPSHOT** 当前开发版本，新功能在这个版本上开发。  

修复线上bug流程：  
1. 以master为基础，本地创建分支，修改bug；
1. 提merge request 到 master
1. review 代码，merge

上线流程：
1. 升级一个小版本号，新版本号为：4.0.x + 0.0.1；
1. 修改root pom.xml 中的version字段为新版本号，并提交到master上；
1. 打一个tag ，名称为新版本号;
1. 打包上线。

开发新功能流程：
1. 以4.1.0-SNAPSHOT 为基础，本地创建分支，开发；
1. 提merge request 到 4.1.0-SNAPSHOT