package com.xiaoju.uemc.tinyid.server.handler;

import com.xiaoju.uemc.tinyid.base.util.TinyIdHttpUtils;
import com.xiaoju.uemc.tinyid.server.config.ZookeeperConfig;
import com.xiaoju.uemc.tinyid.server.factory.impl.IdGeneratorFactoryServer;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ZkMasterSelectHandler
 * @Description: todo
 * @Company: xxxxx
 * @Author: zhengcq
 * @Date: 2020/12/26
 */
@Component
public class ZkMasterSelectHandler implements InitializingBean {

    @Autowired
    private CuratorFramework zkClient;

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    @Autowired
    private IdGeneratorFactoryServer idGeneratorFactoryServer;

    @Value("${server.port}")
    private String port;

    @Getter
    private String nodePath;

    private String localUrl;

    @Getter
    private Boolean isMaster;

    @Getter
    private String masterUrl;

    private String masterNodePath;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!zookeeperConfig.isInZk()) {
            // zk 失效或者连接不上。每个节点都是master 节点。
            System.out.println("zk invalid");
            isMaster = true;
            return;
        }
        Stat stat = zkClient.checkExists().forPath(zookeeperConfig.getTinyIdNodePath());
        // 创建 tiny master zk 节点
        if (stat == null) {
            int i = 1;
            for (;;) {
                try {
                    zkClient.create().withMode(CreateMode.PERSISTENT).forPath(zookeeperConfig.getTinyIdNodePath());
                } catch (Exception e) {
                    stat = zkClient.checkExists().forPath(zookeeperConfig.getTinyIdNodePath());
                    if (stat != null) {
                        break;
                    }
                }
                TimeUnit.SECONDS.sleep(3);
                i++;
                if (i > 3) {
                    break;
                }
            }
        }
        String ipWithPort = getLocalIpWithPort();
        localUrl = ipWithPort;
        nodePath = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(zookeeperConfig.getTinyIdNodePath() + "/node", ipWithPort.getBytes());

        if (checkMaster()) {
            masterNodePath = nodePath;
            masterUrl = ipWithPort;
            System.out.println("isMaster:" + isMaster);
            beMaster();
        } else {
            masterUrl = new String(zkClient.getData().forPath(masterNodePath));
            isMaster = false;
            System.out.println(String.format("isMaser:%s, masterNodePath:%s, masterUrl:%s", isMaster, masterNodePath, masterUrl));
            watchMaster();
        }
    }

    private void toBeMaster() throws Exception {
        if (checkMaster()) {
            masterNodePath = nodePath;
            masterUrl = localUrl;
            System.out.println("isMaster:" + isMaster);
            beMaster();
        }
    }

    private void beMaster() {
        synchronized (isMaster) {
            if (isMaster) {
                return;
            }
            isMaster = true;
            idGeneratorFactoryServer.clearGenerator();
        }

    }

    private void  watchMaster() throws Exception {
        System.out.println("start to watch " + masterNodePath);
        zkClient.checkExists().usingWatcher(new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(" event " + watchedEvent.getType());
                if (watchedEvent.getType().equals(Event.EventType.NodeDeleted)) {
                    try {
                        if (checkMaster()) {
                            masterNodePath = nodePath;
                            masterUrl = localUrl;
                            System.out.println("watch deal");
                            beMaster();

                        } else {
                            watchMaster();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        watchMaster();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).forPath(masterNodePath);
    }

    private   boolean checkMaster() throws Exception {
        List<String> childs = zkClient.getChildren().forPath(zookeeperConfig.getTinyIdNodePath());
        Collections.sort(childs, (o1, o2) -> {
            return o1.compareTo(o2);
        });
        if (childs != null && !childs.isEmpty() && getRealNodeName(nodePath).equals(childs.get(0))) {
            return true;
        }
        masterNodePath = zookeeperConfig.getTinyIdNodePath() + "/" + childs.get(0);
        return false;
    }

    private  String getRealNodeName(String nodePath) {
        String[] tmps = nodePath.split("/");
        return tmps[tmps.length -1];
    }

    private String getLocalIpWithPort() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String ip=addr.getHostAddress().toString();     //ip:192.168.0.104
        return String.format("%s:%s", ip, port);
    }


    public String forwardMaster(String path, Map<String, String> form, boolean tryFlag) throws Exception {
        if (isMaster) {
            return "";
        }
        String url =  String.format("http://%s/tinyid/%s?", masterUrl, path);
        String rs = "";
        try {
            rs = TinyIdHttpUtils.post(url, form, 30, 30);
            System.out.println(rs);
        } catch (Exception e) {
            if (tryFlag) {
                toBeMaster();
                forwardMaster(path, form, false);
            }
        }
        return rs;
    }
}
