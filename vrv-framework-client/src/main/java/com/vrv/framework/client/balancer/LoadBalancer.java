package com.vrv.framework.client.balancer;

import com.vrv.framework.client.loader.NodeLoader;
import com.vrv.framework.client.model.Node;

import java.util.List;

/**
 * 用于封装负载均衡逻辑 <br>
 * 将原来load node节点信息的逻辑提取到{@link NodeLoader}接口中，而LoadBalancer专门服务负载均衡
 * <p>软负载均衡，需实现具体算法，如roundrobin，random等</p>
 *
 * @author chenlong
 * @date 2021/9/9 15:53
 */
public interface LoadBalancer {

    /**
     * 给定serviceId，返回一个负载均衡后的节点
     *
     * @param serviceId
     * @param nodes
     * @return
     */
    public Node getNode(String serviceId, List<Node> nodes);
}
