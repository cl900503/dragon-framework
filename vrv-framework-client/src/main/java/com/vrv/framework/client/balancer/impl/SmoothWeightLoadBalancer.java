package com.vrv.framework.client.balancer.impl;


import com.vrv.framework.client.balancer.LoadBalancer;
import com.vrv.framework.client.model.Node;
import com.vrv.framework.common.exception.VoaNoneAvailableNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * 平滑加权算法
 *
 * @author chenlong
 * @date 2021-08-06 13:47:06
 */
public class SmoothWeightLoadBalancer implements LoadBalancer {

    private static Logger logger = LoggerFactory.getLogger(SmoothWeightLoadBalancer.class);

    /**
     * <servideId,List<Node>>
     */
    private Map<String, List<Node>> nodesMap = new ConcurrentHashMap<>();

    @Override
    public Node getNode(String serviceId, List<Node> nodes) {

        synchronized (serviceId.intern()) {
            // 同步节点信息进nodesMap
            synchronizationNode(serviceId, nodes);

            List<Node> nodeList = nodesMap.get(serviceId);
            if (nodeList == null || nodeList.size() == 0) {
                logger.error("getNode None available node for service : " + serviceId);
                throw new VoaNoneAvailableNodeException("None available node for service: " + serviceId);
            }

            // 求出权重总和
            long weightSum = nodeList.stream().mapToLong(Node::getWeight).sum();

            // 选中节点
            Node smoothWeightNode = null;
            for (Node node : nodeList) {
                // 初始化当前权重为weight
                node.setCurrentWeight(node.getCurrentWeight() + node.getWeight());

                if (smoothWeightNode == null || smoothWeightNode.getCurrentWeight() < node.getCurrentWeight()) {
                    smoothWeightNode = node;
                }
            }
            smoothWeightNode.setCurrentWeight(smoothWeightNode.getCurrentWeight() - weightSum);
            logger.debug("getNode result:{}", smoothWeightNode);
            return smoothWeightNode;
        }

    }

    /**
     * 同步节点信息到nodesMap
     *
     * @param serviceId
     * @param nodes
     * @author chenlong
     * @date 2021-08-06 13:51:31
     */
    private void synchronizationNode(String serviceId, List<Node> nodes) {

        // 传入节点为null
        if (nodes == null) {
            nodesMap.remove(serviceId);
            return;
        }

        // 去除无效节点
        nodes = nodes.stream().filter(node -> !node.isDisabled() && node.isHealthy()).collect(Collectors.toList());

        // map中为null，nodes不为null
        if (nodesMap.get(serviceId) == null) {
            // 直接将nodes丢入
            nodesMap.put(serviceId, nodes);
            return;
        }
        // map中不为null，nodes不为null
        // 比较是否相同
        List<Node> list = nodesMap.get(serviceId);

        if (list.size() != nodes.size()) {
            // 不相同
            nodesMap.remove(serviceId);
            nodesMap.put(serviceId, nodes);
            return;
        }

        // 比对两个集合元素是否相同，比对条件（host,port,weight）
        int count = 0;
        for (Node a : list) {
            for (Node b : nodes) {
                if (a.getHost().equals(b.getHost()) && a.getPort() == b.getPort()
                        && a.getWeight() == b.getWeight()) {
                    count += 1;
                    break;
                }
            }
        }

        if (count != nodes.size()) {
            nodesMap.remove(serviceId);
            nodesMap.put(serviceId, nodes);
        }

    }

}
