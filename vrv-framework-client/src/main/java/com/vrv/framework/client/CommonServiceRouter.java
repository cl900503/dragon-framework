package com.vrv.framework.client;


import com.vrv.framework.client.balancer.LoadBalancer;
import com.vrv.framework.client.balancer.impl.SmoothWeightLoadBalancer;
import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.loader.NodeLoader;
import com.vrv.framework.client.loader.impl.VcsNodeLoader;
import com.vrv.framework.client.model.Node;
import com.vrv.framework.client.provider.ConnectionProvider;
import com.vrv.framework.client.provider.impl.TTransportConnectionProvider;
import com.vrv.framework.common.exception.VoaClientPoolException;
import com.vrv.framework.common.exception.VoaNoneAvailableNodeException;
import com.vrv.framework.common.exception.VoaTransportException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 最核心服务寻址实现，包括查找节点，选举算法以及连接池的操作。
 *  TODO 通过异步定时任务刷新检测故障，这一部分需要重构。
 *
 * @author chenlong
 * @date 2021/9/9 15:09
 */
public class CommonServiceRouter implements ServiceRouter {


    static Logger logger = LoggerFactory.getLogger(CommonServiceRouter.class);

    private static CommonServiceRouter instance = new CommonServiceRouter();

    private ConnectionProvider connectionProvider = new TTransportConnectionProvider();

    //private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();//轮询
    //平滑加权
    private LoadBalancer loadBalancer = new SmoothWeightLoadBalancer();

    private NodeLoader loader = new VcsNodeLoader();

    /**
     * For Unit-test, we need to inject mock fields.
     *
     * @param connectionProvider
     */
    protected void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    ScheduledThreadPoolExecutor timerExecutor = new ScheduledThreadPoolExecutor(4);

    private final static int DISABLE_THRESHOLD = 100;

    private final static int MAX_GET_RETRY = 2;

    private static String localIp = getLocalAddr();

    private AtomicInteger noneTransportCounter = new AtomicInteger();

    private int ERROR_THRESHOLD = 100;

    private static String getLocalAddr() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface ifc = interfaces.nextElement();
            Enumeration<InetAddress> addressesOfAnInterface = ifc.getInetAddresses();

            while (addressesOfAnInterface.hasMoreElements()) {
                InetAddress address = addressesOfAnInterface.nextElement();
                if (address.isSiteLocalAddress()) {
                    return address.getHostAddress();
                }
            }
        }

        return null;
    }

    private CommonServiceRouter() {
        timerExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    ConcurrentMap<String, NodeErrorInfo> cleanedMap = new ConcurrentHashMap<String, NodeErrorInfo>();
                    Collection<NodeErrorInfo> errs = errorMap.values();
                    for (NodeErrorInfo err : errs) {
                        // 轮询Registry所有节点，更新errorMap
                        //XOASEC-148：version不一定是1
                        List<Node> curNodes = loader.loadAll(err.getServiceId(), err.getVersion(), null);
                        for (Node current : curNodes) {
                            String currentNodeKey = current.getNodeKey();
                            String errorNodeKey = err.getNode().getNodeKey();
                            if (currentNodeKey.equals(errorNodeKey)) {
                                cleanedMap.put(currentNodeKey, err);
                            }
                        }
                    }
                    errorMap = cleanedMap;
                    for (Map.Entry<String, NodeErrorInfo> entry : errorMap.entrySet()) {
                        NodeErrorInfo err = entry.getValue();
                        Node n = err.getNode();
                        if (n == null) {
                            continue;
                        }
                        String nodeKey = entry.getKey();
                        if (!n.isHealthy()) {
                            if (n.isDisabled()) {//已经设置不可用了，就不用检查了
                                errorMap.remove(nodeKey);
                                logger.error("remove check disabled node : " + nodeKey + " healthy=" + n.isHealthy()
                                        + " disabled=" + n.isDisabled() + "");
                            } else {
                                // 只有连接成功，才拿实际业务来重试
                                try {
                                    Socket sock = new Socket();
                                    SocketAddress endpoint = new InetSocketAddress(n.getHost(), n
                                            .getPort());
                                    sock.connect(endpoint, 500);
                                    sock.close();
                                } catch (Exception e) {
                                    smsNotify(err.getServiceId(), nodeKey, "connect fail");
                                    logger.error("Disabled xoa2 node " + nodeKey
                                            + " connect fail before retry");
                                    continue;
                                }
                                errorMap.remove(nodeKey);
                                n.setHealthy(true);
                                err.setCount(0);
                                logger.error("Re-enable xoa2 node: " + nodeKey);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
//                                logger.debug("Check " + nodeKey + " healthy=" + n.isHealthy()
//                                        + " disabled=" + n.isDisabled() + " err=" + err.getCount()
//                                        + "/" + DISABLE_THRESHOLD);
                            }
                        }
                    }
                } catch (Throwable e) {
                    logger.error("Node check error", e);
                }
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    public static CommonServiceRouter getInstance() {
        return instance;
    }

    @Override
    public VoaTransport routeService(String serviceId, String version, String shardBy, long timeOut) throws Exception {
        return routeService(serviceId, version, shardBy, timeOut, null);
    }

    @Override
    public VoaTransport routeService(String serviceId, String version, String shardBy, long timeOut, NodeFilter filter) throws Exception {
        Node node = null;
        TTransport transport = null;
        VoaTransport xoaTransport = null;
        int shard;
        try {
            shard = Integer.parseInt(shardBy);
        } catch (Exception e1) {
            shard = 0;
        }
        List<Node> nodes = loader.load(serviceId, version, shard, filter);
        if (logger.isDebugEnabled()) {
            logger.debug("service nodes: size=" + nodes.size() + "");
        }
        int retry = 0;
        while (true) {
            try {
                node = loadBalancer.getNode(serviceId, nodes);
                transport = connectionProvider.getConnection(node, timeOut);
                break;
            } catch (VoaNoneAvailableNodeException e) {
                throw e;
            } catch (Exception e) {

                disableNode(serviceId, version, node, DISABLE_THRESHOLD / 200);
                logger.warn("Get service error : " + node.getNodeKey() + ' ' + serviceId);

                if (++retry >= MAX_GET_RETRY) {
                    throw new VoaClientPoolException("service error : " + node.getNodeKey() + ' ' + serviceId, e);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("get service '" + serviceId + "', node: " + node);
        }
        xoaTransport = new VoaTransport();
        xoaTransport.setNode(node);
        xoaTransport.setTransport(transport);
        return xoaTransport;
    }

    @Override
    public VoaTransport routeService(String serviceId, String version, String ip, int port, String protocol, long timeOut)
            throws Exception {
        //Node node = new Node(ip,port,false,true);
        Node node = new Node(ip, port, "", protocol, 1, false, true);

        TTransport transport = null;
        VoaTransport xoaTransport = null;
        int retry = 0;
        while (true) {
            try {
                transport = connectionProvider.getConnection(node, timeOut);
                break;
            } catch (VoaNoneAvailableNodeException e) {
                throw e;
            } catch (Exception e) {
                //由于调用ap是特定的ip和port的ap,
                disableApNode(serviceId, version, node);
//                disableNode(serviceId, version, node, DISABLE_THRESHOLD / 200);
                logger.warn("Get service by ip and port error : " + node.getNodeKey() + ' ' + serviceId);

                if (++retry >= MAX_GET_RETRY) {
                    throw new VoaClientPoolException("get service by ip and port  error : " + node.getNodeKey() + ' ' + serviceId, e);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("get  service get service by ip and port '" + serviceId + "', node: " + node);
        }
        xoaTransport = new VoaTransport();
        xoaTransport.setNode(node);
        xoaTransport.setTransport(transport);
        return xoaTransport;
    }


    @Override
    public void returnConn(VoaTransport xoaTransport) throws Exception {
        String signature = xoaTransport.getNode().getNodeKey();
        NodeErrorInfo err = errorMap.get(signature);
        if (err != null) {
            err.setCount(0);
        }

        connectionProvider.returnConnection(xoaTransport);
    }


    private ConcurrentMap<String, NodeErrorInfo> errorMap = new ConcurrentHashMap<String, NodeErrorInfo>();

    private ConcurrentMap<String, Long> smsTimestampMap = new ConcurrentHashMap<String, Long>();

    private void smsNotify(String serviceId, String node, String desc) {
        Long lastSentTime = smsTimestampMap.get(serviceId);
        Long currentTime = System.currentTimeMillis();

        if (lastSentTime != null && (currentTime - lastSentTime < 180000)) {
            return;
        }
        smsTimestampMap.put(serviceId, currentTime);
        String msg = "xoa2.0-" + serviceId + "-client=" + localIp + "-service=" + node + "-msg=" + desc;
        doAlarm(serviceId, node, "down", msg);
    }

    private void doAlarm(String serviceId, String node, String state, String msg) {
        /*短信通知暂时没有
    	SmsService svc = SmsServiceFactory.getSmsService();
        List<String> servicePhones = XcsConfigurations.getConfiguration().getServerAlarmPhones();

        if (servicePhones != null) {
            for (String phone : servicePhones) {
                svc.sendSms(phone, node, serviceId, state, msg);
            }
        }
        */
    }

    void disableApNode(String serviceId, String version, Node node) {
        //ap没有注册信息没有放到zk,所以这里直接清除连接池，由于ap是特定的ip和port,就算ap掉线,也得调用他(或者其他特定的节点),不能调用其他任意节点,并且没有监听，不知道ap有没有上线,所以也不做健康度的设置，只做清除连接池的操作
        if (node == null) {
            return;
        }
        connectionProvider.clearConnections(node);
    }

    void disableNode(String serviceId, String version, Node node, int delta) {
        if (node == null) {
            return;
        }
        String signature = node.getNodeKey();
        NodeErrorInfo err = errorMap.get(signature);

        // TODO: 这里需要clear么？有问题吧！需要分情况讨论的
        connectionProvider.clearConnections(node);

        if (err == null) {
            err = new NodeErrorInfo(serviceId, version, node, delta);
            errorMap.put(signature, err);
        } else {
            if (err.getNode() != node) {
                err.setNode(node);
            }
            err.addCount(delta);
        }

        if (logger.isWarnEnabled()) {
            logger.warn(signature + " disabled " + err.getCount() + '/' + DISABLE_THRESHOLD);
        }
        if (err.getCount() >= DISABLE_THRESHOLD) {
            node.setHealthy(false);
            String msg = "service node [" + signature + "] of " + serviceId + "  was disabled.";
            doAlarm(serviceId, signature, "disabled", msg);
        }
    }

    //TODO：处理exception的方法，应该包括所有的xoa客户端的异常类型，因此，所有的xoa客户端异常都应该由该方法处理
    //现在的异常处理比较分散，重新定义各个类型的异常，让exception清晰统一
    @Override
    public void serviceException(String serviceId, String version, Throwable e,
                                 VoaTransport xoaTransport) {
        String signature = "null-service";
        Node node = null;
        if (xoaTransport != null) {
            node = xoaTransport.getNode();
            signature = node.getNodeKey();
            if (logger.isDebugEnabled()) {
                logger.debug("invalidate addr=" + signature + ",prov=" + connectionProvider
                        + ",xoaTransport=" + xoaTransport);
            }
            connectionProvider.invalidateConnection(xoaTransport);
        }

        int delta = 1;

        if (e instanceof TTransportException) {
            Throwable cause = e.getCause();
            if (cause == null) {
                int type = ((TTransportException) e).getType();
                switch (type) {
                    case TTransportException.END_OF_FILE:
                        logger.error("xoa2 service=" + serviceId + " addr=" + signature + " ex="
                                + "RPC TTransportException END_OF_FILE");
                        delta = DISABLE_THRESHOLD / 200;
                        break;
                    default:
                        logger.error("xoa2 service=" + serviceId + " addr=" + signature + " ex="
                                + "RPC TTransportException type=" + type);
                        delta = DISABLE_THRESHOLD / 200;
                        break;
                }
            } else {
                if (cause instanceof java.net.SocketTimeoutException) {
                    delta = DISABLE_THRESHOLD / 500;
                    logger.error("xoa2 service=" + serviceId + " addr=" + signature + " ex="
                            + "RPC TTransportException SocketTimeoutException");
                } else {
                    delta = DISABLE_THRESHOLD / 200;
                    logger.error("xoa2 service=" + serviceId + " addr=" + signature + " ex="
                            + "RPC TTransportException " + cause.getMessage());
                }
            }
        } else if (e instanceof VoaTransportException) {
            delta = DISABLE_THRESHOLD / 200;
            logger.error("xoa2 service=" + serviceId + " addr=" + signature + " ex="
                    + "XoaTransportException " + e.getMessage());
        } else if (e instanceof VoaClientPoolException) {
            logger.error("xoa2 service=" + serviceId + " addr=" + signature + " ex="
                    + "XoaClientPoolException");
            delta = 1;
        } else if (e instanceof VoaNoneAvailableNodeException) {
            //如果无可用transport累计一定次数，发报警短信，通知的是service developer
            if (noneTransportCounter.addAndGet(1) % ERROR_THRESHOLD == 0) {
                String message = "No transport avalible for " + serviceId + " version " + version + " at " + localIp;
                doAlarm(serviceId, localIp, "none node", message);
                ERROR_THRESHOLD = ERROR_THRESHOLD * 2 > 10000 ? 100 : ERROR_THRESHOLD * 2;
                noneTransportCounter.set(0);
            }
            return;
        }

        if (delta <= 0) {
            delta = 1;
        }

        disableNode(serviceId, version, node, delta);
    }

    /**
     * ap专用
     *
     * @param serviceId
     * @param version
     * @param frameworkException
     * @param xoaTransport
     */
    @Override
    public void serviceApException(String serviceId, String version, Throwable frameworkException, VoaTransport xoaTransport, String ip,
                                   int port) {
        Node node = new Node(ip, port);
        disableApNode(serviceId, version, node);
    }

    /**
     * 路由 CEMS 服务节点
     */
    @Override
    public List<VoaTransport> routeCemsService(String serviceId, String version, long timeOut)
            throws Exception {
        // TODO Auto-generated method stub
        return routeCemsService(serviceId, version, timeOut, null);
    }

    /**
     * 路由 CEMS 服务节点
     */
    @Override
    public List<VoaTransport> routeCemsService(String serviceId, String version, long timeOut, NodeFilter filter) throws Exception {
        List<VoaTransport> voaTransportList = new ArrayList<VoaTransport>();
        Node node = null;
        TTransport transport = null;
        VoaTransport xoaTransport = null;
        List<Node> nodes = loader.loadAll(serviceId, version, filter);
        if (logger.isDebugEnabled()) {
            logger.debug("service nodes: size=" + nodes.size() + "");
        }
        int retry = 0;
        for (int i = 0; i < nodes.size(); i++) {
            try {
                node = nodes.get(i);
                if (node.isDisabled() || !node.isHealthy()) {
                    continue;
                }
                transport = connectionProvider.getConnection(node, timeOut);
                xoaTransport = new VoaTransport();
                xoaTransport.setNode(node);
                xoaTransport.setTransport(transport);
                voaTransportList.add(xoaTransport);
            } catch (VoaNoneAvailableNodeException e) {
                throw e;
            } catch (Exception e) {
                disableNode(serviceId, version, node, DISABLE_THRESHOLD / 200);
                logger.warn("Get service error : " + node.getNodeKey() + ' ' + serviceId);

                if (++retry >= MAX_GET_RETRY) {
                    throw new VoaClientPoolException("service error : " + node.getNodeKey() + ' ' + serviceId, e);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("get service '" + serviceId + "', node: " + node);
        }

        return voaTransportList;
    }


}
