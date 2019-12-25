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
package org.joyqueue.toolkit.network;

import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.network.Lan;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by hexiaofeng on 16-5-7.
 */
public class IpUtilTest {

    @Test
    public void showDefaultLocalIp() {
        System.out.println(IpUtil.getLocalIp());
    }

  /*  @Test
    public void testIsIp() {
        Assert.assertTrue(IpUtil.isIp("127.0.0.1"));
        Assert.assertTrue(IpUtil.isIp("255.255.255.255"));
        Assert.assertTrue(IpUtil.isIp("192.168.1.1"));
        Assert.assertFalse(IpUtil.isIp("192..168.1.1"));
        Assert.assertFalse(IpUtil.isIp("0000000001.168.1.1"));
        Assert.assertFalse(IpUtil.isIp("asc.0.0.1"));
        Assert.assertFalse(IpUtil.isIp("1asc.0.0.1"));
        Assert.assertFalse(IpUtil.isIp("123455.0.0.1"));
        Assert.assertFalse(IpUtil.isIp("00000123455.0.0.1"));
        Assert.assertFalse(IpUtil.isIp("155.0.0.1.1"));
        Assert.assertFalse(IpUtil.isIp("192.168.1.1."));
        Assert.assertFalse(IpUtil.isIp("192.168.1"));
        Assert.assertFalse(IpUtil.isIp("192.168.1."));
    }

    @Test
    public void testIsAddress() {
        Assert.assertTrue(IpUtil.isAddress("127.0.0.1"));
        Assert.assertTrue(IpUtil.isAddress("127.0.0.1:1"));
        Assert.assertTrue(IpUtil.isAddress("127.0.0.1:65535"));
        Assert.assertTrue(IpUtil.isAddress("127.0.0.1_65535"));
        Assert.assertTrue(IpUtil.isAddress("127.0.0.1.65535"));
        Assert.assertTrue(IpUtil.isAddress("127_0_0_1_65535"));
        Assert.assertTrue(IpUtil.isAddress("127_0_0_1:65535"));
        Assert.assertTrue(IpUtil.isAddress("127.0_0:1_65535"));
        Assert.assertTrue(IpUtil.isAddress("255.255.255.255"));
        Assert.assertTrue(IpUtil.isAddress("192.168.1.1"));
        Assert.assertFalse(IpUtil.isAddress("192..168.1.1"));
        Assert.assertFalse(IpUtil.isAddress("0000000001.168.1.1"));
        Assert.assertFalse(IpUtil.isAddress("asc.0.0.1"));
        Assert.assertFalse(IpUtil.isAddress("1asc.0.0.1"));
        Assert.assertFalse(IpUtil.isAddress("123455.0.0.1"));
        Assert.assertFalse(IpUtil.isAddress("00000123455.0.0.1"));
        Assert.assertFalse(IpUtil.isAddress("155.0.0.1.1.1"));
        Assert.assertFalse(IpUtil.isAddress("192.168.1.1."));
        Assert.assertFalse(IpUtil.isAddress("192.168.1"));
        Assert.assertFalse(IpUtil.isAddress("192.168.1."));
        Assert.assertFalse(IpUtil.isAddress("192.168.1.2:65536"));
        Assert.assertFalse(IpUtil.isAddress("192.168.1.2:65x"));
    }*/

    @Test
    public void testIpLong() {
        String ip = "172.1.1.1";
        long value = IpUtil.toLong(ip);
        String ip1 = IpUtil.toIp(value);
        Assert.assertEquals(ip, ip1);
    }

    @Test
    public void testLan() {
        Lan lan = new Lan("172.168.1.0/24;10.1.1.0/24");
        Assert.assertTrue(lan.contains("172.168.1.0"));
        Assert.assertTrue(lan.contains("172.168.1.255"));
        Assert.assertTrue(lan.contains("10.1.1.0"));
        Assert.assertTrue(lan.contains("10.1.1.255"));
        Assert.assertEquals(lan.toString(), "172.168.1.0-172.168.1.255;10.1.1.0-10.1.1.255");
        lan = new Lan("172.168.1.0-172.168.1.5");
        Assert.assertTrue(lan.contains("172.168.1.0"));
        Assert.assertTrue(lan.contains("172.168.1.5"));
        Assert.assertFalse(lan.contains("172.168.1.255"));
        lan = new Lan("172.168.1.1");
        Assert.assertTrue(lan.contains("172.168.1.1"));
        Assert.assertFalse(lan.contains("172.168.1.255"));
        lan = new Lan("172.168.1.*");
        Assert.assertTrue(lan.contains("172.168.1.0"));
        Assert.assertTrue(lan.contains("172.168.1.255"));
        Assert.assertFalse(lan.contains("172.168.2.255"));
    }

    @Test
    public void testByte() throws SocketException {
        String localIp = IpUtil.getLocalIp();
        byte[] data = IpUtil.toByte(localIp);
        String ip = IpUtil.toIp(data);
        Assert.assertEquals(ip, localIp);
        ip = IpUtil.toIp(data);
        Assert.assertEquals(ip, localIp);

        InetSocketAddress address = new InetSocketAddress(localIp, 1158);
        String addr = IpUtil.toAddress(address);
        Assert.assertEquals(addr, localIp + ":" + 1158);
        data = IpUtil.toByte(address);
        InetSocketAddress address1 = IpUtil.toAddress(data);
        Assert.assertEquals(address, address1);
    }
}
