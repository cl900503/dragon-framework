package com.vrv.framework.client.router.impl;

import com.vrv.framework.client.model.Node;

/**
 * @author chenlong
 * @date 2021/9/9 16:10
 */
public class NodeErrorInfo {

    private String serviceId;

    private String version;

    private int count;

    private Node node;

    NodeErrorInfo(String serviceId, String version, Node node, int count) {
        this.serviceId = serviceId;
        this.version = version;
        this.count = count;
        this.node = node;
    }

    void addCount(int delta) {
        count += delta;
    }

    String getServiceId() {
        return serviceId;
    }

    String getVersion() {
        return version;
    }

    Node getNode() {
        return node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(Node node) {
        this.node = node;
    }

    int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

}
