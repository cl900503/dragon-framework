package com.vrv.framework.client.registry;


import com.vrv.framework.client.router.impl.CommonServiceRouter;
import com.vrv.framework.client.provider.ConnectionProvider;
import com.vrv.framework.client.model.Node;
import com.vrv.framework.client.model.Service;
import com.vrv.framework.client.model.Shard;
import com.vrv.framework.common.utils.json.JacksonUtil;
import com.vrv.framework.registry.model.ServiceConfigBean;
import com.vrv.framework.registry.service.ConfigServerFactoryUtil;
import com.vrv.framework.registry.utils.zk.ZookeeperHelp;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 新版获取节点配置
 *
 * @author chengnl
 */
public class VoaRegistryImplNew implements VoaRegistry {
//	private static Log logger = LogFactory.getLog(VoaRegistryImplNew.class.getName());

    private static Logger logger = LoggerFactory.getLogger(VoaRegistryImplNew.class);

    private ConcurrentHashMap<String, Service> serviceMap = new ConcurrentHashMap<String, Service>();
    private static ScheduledThreadPoolExecutor timerExecutor = new ScheduledThreadPoolExecutor(1);
    private int defaultShard = 0;// shard 分片 暂时用不到，默认设置为0

    public VoaRegistryImplNew() {
        // 启动定时检测服务状态任务
        checkServiceState();
    }

    @Override
    public Service queryService(String serviceId, String version) {
        Service service = serviceMap.get(constructAccessorKey(serviceId, version));
        if (service == null) {
            synchronized (constructAccessorKey(serviceId, version).intern()) {
                service = serviceMap.get(constructAccessorKey(serviceId, version));
                if (null != service) {
                    return service;
                }
                service = new Service(serviceId, version, null, null);
//					serviceMap.put(constructAccessorKey(serviceId,version),service);
                // 加载节点
                try {
                    /*
                     * ClientHandler client = ConfigServerUtil.getConfigServiceClient();
                     * List<ServiceConfigBean> list
                     * =(List<ServiceConfigBean>)client.invokeHandler("queryService", new
                     * Object[]{serviceId,version});
                     */
                    List<ServiceConfigBean> list = ConfigServerFactoryUtil.getConfigService().queryService(serviceId, version);
                    if (list != null && list.size() > 0) {
                        for (ServiceConfigBean config : list) {
                            registerNode(service, config);
                        }
                        serviceMap.put(constructAccessorKey(serviceId, version), service);
                    }
                } catch (Exception e) {
                    logger.error("ConfigServerUtil queryService  error：" + e.getMessage());
                }
            }
        }
        return service;
    }

    @Override
    public void registerNode(Service service, int shard, Node node) {
        synchronized (constructAccessorKey(service.getId(), service.getVersion()).intern()) {
            Shard shardObj = service.getShards().get(shard);
            if (shardObj == null) {
                List<Node> nodelist = new ArrayList<Node>();
                nodelist.add(node);
                Shard shardNew = new Shard(service, shard, nodelist);
                service.getShards().put(shard, shardNew);
            } else {
                boolean isHav = false;
                List<Node> nodes = shardObj.getOriginNodes();
                if (nodes.size() > 0) {
                    for (Node n : nodes) {
                        if (n.getHost().equals(node.getHost()) && n.getPort() == node.getPort()) {
                            isHav = true;
                            // 如果已经存在更新协议信息
                            n.setNetwork(node.getNetwork());
                            n.setProtocol(node.getProtocol());
                            n.setWeight(node.getWeight());
                            break;
                        }
                    }
                }
                if (!isHav) {
                    nodes.add(node);
                }
            }
        }
        logger.info("register a node: " + node + " to service: " + service);
    }

    public void registerNode(Service service, ServiceConfigBean config) {
        Node node = new Node(config.getIp(), config.getPort(), config.getNetwork(), config.getProtocol(), config.getWeight(), false, true);
        registerNode(service, defaultShard, node);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    // TODO 后期换成zk监听和关注某服务observer整合到一起 chenlong
    private void checkServiceState() {
        timerExecutor.scheduleWithFixedDelay(() -> {
            // 加载节点
            try {
                Set<Map.Entry<String, Service>> set = serviceMap.entrySet();
                for (Iterator<Map.Entry<String, Service>> it = set.iterator(); it.hasNext(); ) {
                    Map.Entry<String, Service> entry = it.next();
                    Service service = entry.getValue();
                    // ClientHandler client = ConfigServerUtil.getConfigServiceClient();
                    List<ServiceConfigBean> list = ConfigServerFactoryUtil.getConfigService().queryService(service.getId(),
                            service.getVersion());
                    synchronized (service) {
                        Shard shard = service.getShards().get(defaultShard);
                        if (shard != null) {
                            List<Node> nodeList = shard.getNodes();
                            if (list == null || list.size() == 0) {
                                setAllNodeDisable(nodeList);
                            } else {
                                // 设置不可用节点
                                checkNodeDisable(nodeList, list);
                                // 添加更新节点
                                checkUpdateNode(service, nodeList, list);
                            }
                        } else if (list != null && list.size() > 0) {
                            for (ServiceConfigBean config : list) {
                                registerNode(service, config);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("ConfigServerUtil checkServiceState  error：" + e.getMessage(), e.getCause());
            }
        }, 3, 1, TimeUnit.SECONDS);// 3秒后，每秒钟刷新一次
    }

    private void setAllNodeDisable(List<Node> nodeList) {
        for (Node node : nodeList) {
            if (!node.isDisabled() || node.isHealthy()) {
                node.setDisabled(true);
                node.setHealthy(false);
                if (logger.isInfoEnabled()) {
                    logger.info("ConfigServerUtil setAllNodeDisable  set service node disabled " + node.toString());
                }
            }
        }
    }

    private void checkNodeDisable(List<Node> nodeList, List<ServiceConfigBean> list) {
        for (Node node : nodeList) {
            boolean isHav = false;
            for (ServiceConfigBean config : list) {
                if (config.getIp().equals(node.getHost()) && config.getPort() == node.getPort()) {
                    isHav = true;
                    break;
                }
            }
            if (!isHav) {
                if (!node.isDisabled() || node.isHealthy()) {// TODO 动态端口以后,这里可能就是直接删除节点
                    node.setDisabled(true);
                    node.setHealthy(false);
                    if (logger.isInfoEnabled()) {
                        logger.info("ConfigServerUtil checkNodeDisable  set service node disabled " + node.toString());
                    }
                }
            }
        }
    }

    /**
     * 如果存在则忽略，不存在则新增
     *
     * @param service
     * @param nodeList 现有nodes信息
     * @param list     新查出来的节点信息
     * @author chenlong
     * @date 2021-06-23 10:34:22
     */
    private void checkUpdateNode(Service service, List<Node> nodeList, List<ServiceConfigBean> list) {
        for (ServiceConfigBean config : list) {
            boolean isHav = false;
            for (Node node : nodeList) {
                // TODO 动态端口以后,这里可能就是直接添加
                if (config.getIp().equals(node.getHost()) && config.getPort() == node.getPort()) {
                    node.setDisabled(false);
                    node.setHealthy(true);
                    node.setNetwork(config.getNetwork());
                    node.setProtocol(config.getProtocol());
                    node.setWeight(config.getWeight());
//		                node.setProperty(config.getProperty());
                    isHav = true;
                    break;
                }
            }
            if (!isHav) {
                registerNode(service, config);
            }
        }
    }

    private String constructAccessorKey(String serviceId, String version) {
        return new StringBuffer().append(serviceId).append("-").append(version).toString();
    }

    @Override
    public void observer(String name, String version, long timeout) {
        // zk客户端
        CuratorFramework client = ZookeeperHelp.getConnect();
        TreeCache treeCache = new TreeCache(client, ZookeeperHelp.getNamespace() + "/" + name + "/" + version);

        TreeCacheListener treeCacheListener = (client1, event) -> {
            ChildData data = event.getData();
            if (data != null) {

                // 去掉namespace部分
                String path = StringUtils.substringAfter(data.getPath(), ZookeeperHelp.getNamespace() + "/");
                if (StringUtils.isEmpty(path)) {
                    return;
                }

                // 直接按路径来分割了
                String[] paths = StringUtils.split(path, "/");
                if (paths.length != 3) {// 如果是服务节点，应该是3，如：userServer/1.0/172.16.8.153:11241:1
                    return;
                }

                // 服务节点信息
                ServiceConfigBean serviceConfigBean = JacksonUtil.parseObject(new String(data.getData()), ServiceConfigBean.class);

                // 下面进行的是服务信息发生变更时要做的动作
                switch (event.getType()) {
                    case NODE_ADDED:

                        // serviceMap key
                        String serviceKey = constructAccessorKey(name, version);
                        Service service = serviceMap.get(serviceKey);
                        if (service == null) {
                            synchronized (serviceKey.intern()) {
                                service = serviceMap.get(serviceKey);
                                if (service == null) {
                                    service = new Service(name, version, null, null);
                                    // 新增节点
                                    registerNode(service, serviceConfigBean, timeout);
                                    // 初始化服务节点信息
                                    serviceMap.put(serviceKey, service);
                                } else {
                                    // 新增节点
                                    registerNode(service, serviceConfigBean, timeout);
                                }
                            }
                        } else {
                            registerNode(service, serviceConfigBean, timeout);
                        }

                        break;
                    case NODE_UPDATED:
//					System.out.println("更新服务:" + serviceConfigBean);
                        break;
                    case NODE_REMOVED:
//					System.out.println("移除服务:" + serviceConfigBean);
                        break;
                    default:
                        break;
                }
            } else {

            }
        };

        treeCache.getListenable().addListener(treeCacheListener);

        try {
            treeCache.start();
        } catch (Exception e) {
            logger.error("observer exception:", e);
        }

    }

    public void registerNode(Service service, ServiceConfigBean serviceConfigBean, long timeout) {

        Node node = new Node(serviceConfigBean.getIp(), serviceConfigBean.getPort(), serviceConfigBean.getNetwork(),
                serviceConfigBean.getProtocol(), serviceConfigBean.getWeight(), false, true);

        // 缓存节点信息
        registerNode(service, defaultShard, node);
        // 初始化连接池
        CommonServiceRouter commonServiceRouter = CommonServiceRouter.getInstance();
        ConnectionProvider connectionProvider = commonServiceRouter.getConnectionProvider();
        connectionProvider.initConnectionPool(node, timeout);

    }

}
