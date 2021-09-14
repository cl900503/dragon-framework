package com.vrv.framework.client.registry;


import com.vrv.framework.client.model.Node;
import com.vrv.framework.client.model.Service;

/**
 * TODO 自注册返回路径，添加shutdown时remove操作
 *
 * @author chenlong
 */
public interface VoaRegistry {

    /**
     * 向一个Service中注册一个节点。
     * 此接口由 Server 模块调用
     *
     * @param service
     * @param shard
     * @param node
     */
    public void registerNode(Service service, int shard, Node node);

    /**
     * @param serviceId 服务ID
     * @param version   服务的版本，支持表达式，如:1+
     * @return
     */
    public Service queryService(String serviceId, String version);

    /**
     * 销毁XoaRegistry，取消所有发布的Service和已经注册的节点。
     * 此接口在关闭xoa2-server的时候调用。
     * 此接口由关闭后门调用
     */
    public void destroy();

    /**
     * 关注某服务
     * 提前维护好node信息和创建连接池
     *
     * @param name
     * @param version
     * @param timeout 连接池超时时间
     * @author chenlong
     * @date 2021-06-23 17:05:16
     */
    void observer(String name, String version, long timeout);

}
