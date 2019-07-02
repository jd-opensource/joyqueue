#JoyQueue CI/CD 集成测试

 本地测试，在项目的根目录下
 
``` 

 mvn  -Dmaven.test.skip=true -P docker install 
 cd docker/bin
 python3 ./integration/bootstrap.py  -s ./   -b  $(pwd)/integration/benchmark
 
 
```