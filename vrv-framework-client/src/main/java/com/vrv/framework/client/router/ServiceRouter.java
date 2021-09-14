package com.vrv.framework.client.router;

import com.vrv.framework.client.model.VoaTransport;
import com.vrv.framework.client.filter.NodeFilter;

import java.util.List;

/**
 * 服务路由器。
 * 依赖zk管理服务节点，寻址算法本地实现。
 *
 * @author chenlong
 * @date 2021/9/9 15:00
 */
public interface ServiceRouter {


    /**
     * 根据过滤节点指定service路由
     *
     * @param serviceId
     * @return
     */
    public VoaTransport routeService(String serviceId, String version, String shardBy, long timeOut, NodeFilter filter) throws Exception;

    public VoaTransport routeService(String serviceId, String version, String ip, int port, String protocol, long timeOut) throws Exception;

    /**
     * 获取到指定service的路由
     *
     * @param serviceId
     * @return
     */
    public VoaTransport routeService(String serviceId, String version, String shardBy, long timeOut) throws Exception;

    /**
     * 根据过滤节点指定service路由
     *
     * @param serviceId
     * @return
     */
    public List<VoaTransport> routeCemsService(String serviceId, String version, long timeOut, NodeFilter filter) throws Exception;

    /**
     * 获取到指定service的路由
     *
     * @param serviceId
     * @return
     */
    public List<VoaTransport> routeCemsService(String serviceId, String version, long timeOut) throws Exception;

    /**
     * 将transport连接归还到连接池
     *
     * @param xoaTransport
     * @throws Exception
     */
    public void returnConn(VoaTransport xoaTransport) throws Exception;

    /**
     * 客户端调用出现异常情况
     *
     * @param serviceId
     * @param version
     * @param e
     * @param xoaTransport
     */
    public void serviceException(String serviceId, String version, Throwable e, VoaTransport xoaTransport);

    /**
     * ap专用
     *
     * @param serviceId
     * @param version
     * @param frameworkException
     * @param xoaTransport
     * @param ip
     * @param port
     */
    void serviceApException(String serviceId, String version, Throwable frameworkException, VoaTransport xoaTransport, String ip, int port);

//    /**
//     * 路由Service，建立连接和传输数据时。
//     *
//     * TODO: 其实不应该这样搞，只是为了不改变routeService接口参数。
//     * routeService 应该 关心timeout，因为路由服务、建立连接的时候才有超时这一说。 by Xun Dai
//     *
//     * @param timeout 超时时间millisecond.
//     * @see TSocket#setTimeout(int)
//     *
//     */
//    public void setTimeout(long timeout);

}
