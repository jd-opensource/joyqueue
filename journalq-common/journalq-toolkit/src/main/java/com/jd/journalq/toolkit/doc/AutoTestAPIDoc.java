package com.jd.journalq.toolkit.doc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * Auto test api doc
 *
 **/
public class AutoTestAPIDoc implements HeuristicAutoTest<APIDoc> {
    private Properties properties;
    private String host;
    private final String GET = "GET";
    private final String POST = "POST";
    private final String PUT = "PUT";
    private final String DELETE = "DELETE";

    public AutoTestAPIDoc(Properties properties, String host) {
        this.properties = properties;
        this.host = host;
    }
    @Override
    public TestCase test(List<Class> nowRowParamClasses, APIDoc apiDoc) throws Exception {
       String pathTemplate= apiDoc.getPath();
       if(apiDoc.getParams()!=null){
           StringBuilder params=null;
           for(Param m:apiDoc.getParams()){
                String restKey=":"+m.getName();
                if(pathTemplate.indexOf(restKey)>0) {
                    pathTemplate=pathTemplate.replace(restKey, (String) properties.get(m.getName()));
                }else{
                    if(params==null){
                        params=new StringBuilder();
                        params.append("?");
                    }
                    params.append(m.getName()).append("=").append((String) properties.get(m.getName())).append("&");
                }
           }
           if(params!=null&&params.length()>1){
               params.deleteCharAt(params.length()-1);// &
               pathTemplate+=params.toString();
           }
       }
       return curl(host,pathTemplate,apiDoc.getHttpMethod(),null);
    }

    private  TestCase curl(String host, String path, String method, String body) throws Exception {
        Process process=null;
        TestCase testCase=new TestCase();
        switch (method) {
            case GET:
                testCase.setRequest("curl -X GET "+host+path);
                process=Runtime.getRuntime().exec(testCase.getRequest());
                break;
            case POST:
                break;
            case PUT:
                break;
            case DELETE:
                break;
            default:
                // not found
                break;
        }
        if(process!=null){
            process.waitFor(10, TimeUnit.MILLISECONDS);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result=new StringBuilder();
            String line;
            while (( line = in.readLine()) != null) {
                result.append(line);
            }
            testCase.setResponse(result.toString());
        }
        return testCase;
    }
}


