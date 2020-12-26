package com.xiaoju.uemc.tinyid.server.config;

import lombok.Getter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @ClassName: ZookeeperConfig
 * @Description: todo
 * @Company: xxxxx
 * @Author: zhengcq
 * @Date: 2020/12/26
 */
@Configuration
public class ZookeeperConfig {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperConfig.class);


    @Autowired
    private Environment environment;

    /**
     *  zk  服务地址
     */
    @Value("${zookeeper.service.url}")
    private String zkServiceUrl;

    @Getter
    @Value("${zookeeper.service.node.path}")
    private String tinyIdNodePath;

    @Getter
    private boolean inZk;

    @Bean
    public CuratorFramework zkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client =
                CuratorFrameworkFactory.builder()
                        .connectString(zkServiceUrl)
                        .sessionTimeoutMs(5000)
                        .connectionTimeoutMs(5000)
                        .retryPolicy(retryPolicy)
                        .build();
        try {
            boolean isConnect = false;
            String[] tmps = zkServiceUrl.split(",");
            for (String hostTmp : tmps) {
                if (isHostConnectable(hostTmp)) {
                    isConnect = true;
                    break;
                }
            }
            if (!isConnect) {
                inZk = false;
                return CuratorFrameworkFactory.builder().build();
            }
            client.start();
            inZk = true;
            return client;
        } catch (Exception e) {
            inZk = false;
            return client;
        }
    }


    /**
     * 测试ip 端口是否连通
     *
     * @param url url
     * @return boolean 是否连通
     */
    private boolean isHostConnectable(String url) {
        if (url == null || url.isEmpty() || !url.contains(":")) {
            return false;
        }
        String[] tmps = url.split(":");
        if (tmps.length != 2) {
            return false;
        }
        String host = tmps[0];
        Integer port = Integer.parseInt(tmps[1]);

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 3000);
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
