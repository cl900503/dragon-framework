package com.vrv.framework.client.filter;

import com.vrv.framework.client.model.Node;

import java.util.List;

/**
 * 节点过滤,业务根据自己的需要过滤服务节点
 *
 * @author chenlong
 * @date 2021/9/9 14:55
 */
public interface NodeFilter {
    /**
     * 过滤节点
     *
     * @param nodes
     * @return
     */
    public List<Node> filterNode(List<Node> nodes);
}
