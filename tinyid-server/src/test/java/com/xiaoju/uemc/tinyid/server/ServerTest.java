package com.xiaoju.uemc.tinyid.server;

import com.xiaoju.uemc.tinyid.base.generator.IdGenerator;
import com.xiaoju.uemc.tinyid.server.factory.impl.IdGeneratorFactoryServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @Author du_imba
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class ServerTest {

    @Autowired
    IdGeneratorFactoryServer idGeneratorFactoryServer;

    @Test
    public void testNextId() {
//        IdGenerator idGenerator = idGeneratorFactoryServer.getIdGenerator("test");
//        Long id = idGenerator.nextId();
//        System.out.println("current id is: " + id);
    }

    @Test
    public void testIp() {

        Enumeration<NetworkInterface> allNetInterfaces = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                            && ip.getHostAddress().indexOf(":") == -1) {
                        System.out.println("本机的IP = " + ip.getHostAddress());

                    }

                }
            }

            InetAddress addr = InetAddress.getLocalHost();
            String ip=addr.getHostAddress().toString();     //ip:192.168.0.104
            System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
