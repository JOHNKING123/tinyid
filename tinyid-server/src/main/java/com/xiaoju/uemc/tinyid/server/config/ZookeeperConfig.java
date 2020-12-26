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

/**
 * @ClassName: ZookeeperConfig
 * @Description: todo
 * @Company: 广州市两棵树网络科技有限公司
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
        client.start();
        return client;
    }
}
