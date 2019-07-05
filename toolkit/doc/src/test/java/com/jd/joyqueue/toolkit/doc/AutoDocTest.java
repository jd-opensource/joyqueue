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
package com.jd.joyqueue.toolkit.doc;

import com.alibaba.fastjson.JSON;
import com.jd.joyqueue.toolkit.doc.format.MarkdownAPIDocFormat;
import com.jd.joyqueue.toolkit.doc.vertx.RoutingParser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Ignore
public class AutoDocTest {
    @Test
    public void autoDocTest() throws Exception {
        String pkgName = "com.jd.joyqueue.broker.manage.service";
        String pkgNameB="com.jd.joyqueue.broker.monitor.service";
        String routPath= "manage/routing.xml";
        String defaultProperties="auto_doc.properties";
        String host="http://127.0.0.1:50090";
        List<String> pkgNames=new ArrayList<>();
        List<String> routes=new ArrayList<>();
        pkgNames.add(pkgName);
        pkgNames.add(pkgNameB);
        routes.add(routPath);
        String current=System.getProperty("user.dir");
        AutoDoc autoDoc=new AutoDoc(new File(current+"/../../docs/cn/manage_monitor_auto_doc.md"),routes,pkgNames);
        autoDoc.write(new MarkdownAPIDocFormat(), RoutingParser.class,new AutoTestAPIDoc(parse(defaultProperties),host));
        for(Param m:autoDoc.getParamKeys()){
            System.out.println(JSON.toJSONString(m));
        }
        autoDoc.close();
    }

    /**
     * Parse properties from file
     *
     **/
    public Properties parse(String file){
        Properties properties = new Properties();
        InputStream inputStream =getClass().getClassLoader().getResourceAsStream(file);
        try  {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("加载配置失败", e);
        }
        return  properties;

    }


    @Test
    public void pkgMethodParamType(){
        String pkgName = "com.jd.joyqueue.broker.monitor.service";
        PackageDocScanParser parser=new PackageDocScanParser(pkgName);
        Map<String,Class> classMap=parser.packageScan();
        for(Map.Entry<String,Class> e:classMap.entrySet()){
            Method[] method=e.getValue().getDeclaredMethods();
            System.out.println(e.getValue().toString()+" methods:");
            for(Method m:method){
                m.getName();
                Class[] classes=  m.getParameterTypes();
                for(Class c:classes){
                    System.out.println(c.getSimpleName());
                }
            }

        }
        List<Class> classes=parser.getNonRawParams("com.jd.joyqueue.broker.monitor.service.TopicMonitorService","getTopicInfoByTopics");
        Assert.assertEquals("[interface java.util.List]",classes.toString());
    }
}

