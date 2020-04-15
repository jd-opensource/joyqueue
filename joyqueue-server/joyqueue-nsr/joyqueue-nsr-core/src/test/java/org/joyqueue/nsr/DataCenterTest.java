package org.joyqueue.nsr;


import org.joyqueue.domain.DataCenter;
import org.joyqueue.nsr.util.DCWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class DataCenterTest {
    private List<DCWrapper> dataCenters=new ArrayList();
    int max=255;
    int step=2000;
    int  multi=256*256-1;
    String startSuffix="0.0";
    String endSuffix="255.255";
    Random rand=new Random();
    @Before
    public void init(){
        for(int i=0;i<=multi;i++) {
            int start=i;
                i+=step;
            int   end=Math.min(i,multi);
            String name=String.format("%d-%d",start,end);
            int y=start&0xff;
            int x=(start&0xff00)>>8;
            int ey=end&0xff;
            int ex=(end&0xff00)>>8;
             String url=String.format("IPRANGE://?pattern=%d.%d.%s-%d.%d.%s",x,y,startSuffix,ex,ey,endSuffix);
             dataCenters.add(new DCWrapper(new DataCenter(name, name, name, url)));
        }
    }


    @Test
    public void dcTest(){
//        for(DataCenter dataCenter:dataCenters){
//            System.out.println(JSON.toJSONString(dataCenter));
//        }
        int testN=10000;
        for(int i=0;i<testN;i++){
           String rip= randomIp();
            DataCenter dc=matchDataCenterAndCount(rip);
            int ipIndex=dc.getUrl().indexOf("=");
            String ipRange=dc.getUrl().substring(ipIndex+1);
            String[] ipArray=ipRange.split("-");
            Assert.assertTrue(between(ipArray[0],ipArray[1],rip));
        }
    }

    /**
     * IP compare
     **/
    public boolean between(String startIp,String endIp,String curIp){
        long start=ipToLong(startIp);
        long end=ipToLong(endIp);
        long cur=ipToLong(curIp);
        if(cur>=start&&cur<=end){
            return true;
        }
        return false;
    }

    /**
     * IP to int
     **/
    public long ipToLong(String ip){
       String[] arr= ip.split("\\.");
       long sum=0;
       for(String seg:arr){
           sum=(sum+Integer.valueOf(seg))<<8;
       }
        return sum>>8;
    }


    /**
     *  Match data center count
     *
     **/
    public DataCenter matchDataCenterAndCount(String ip){
       List<DCWrapper> all= dataCenters.stream().filter(dc->dc.match(ip)).collect(Collectors.toList());
       Assert.assertEquals(all.size(),1);
       return all.get(0).getDataCenter();
    }


    /**
     *  Random IP
     **/
    public  String randomIp(){
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<4;i++) {
            int r=rand.nextInt(max + 1);
            builder.append(r).append('.');
        }
        return builder.substring(0,builder.length()-1);
    }
}
